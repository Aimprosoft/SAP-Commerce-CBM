package com.aimprosoft.importexportcloud.service.storage.impl;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.data.CloudObjectData;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.providers.DropboxConnectionProvider;
import com.aimprosoft.importexportcloud.service.storage.DropBoxBatchUploadStrategy;
import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.aimprosoft.importexportcloud.constants.ImportexportcloudConstants.IEM_TRANSMIT_FILE_EXTENSION;
import static com.aimprosoft.importexportcloud.constants.ImportexportcloudConstants.STORAGE_PATH_SEPARATOR;
import static com.aimprosoft.importexportcloud.service.storage.impl.DefaultDropBoxBatchUploadStrategy.CHUNKED_UPLOAD_CHUNK_SIZE;


public class DropBoxStorageService extends AbstractStorageService
{
	private static final String ROOT = "";

	private static final Logger LOGGER = Logger.getLogger(DropBoxStorageService.class);

	private String clientIdentifier;

	private Converter<Metadata, CloudObjectData> fileDataConverter;

	private Converter<Metadata, CloudObjectData> folderDataConverter;

	private DropboxConnectionProvider dropboxConnectionProvider;

	private DropBoxBatchUploadStrategy dropBoxBatchUploadStrategy;

	@Override
	public TaskInfoData download(final TaskInfoData taskInfoData) throws CloudStorageException
	{
		final String filePath = taskInfoData.getCloudFileDownloadPath();
		final DbxClientV2 client = getClient(taskInfoData.getConfig());
		final String dropBoxFilePath = resolveDropBoxPath(filePath);

		try (final DbxDownloader<FileMetadata> download = client.files().download(dropBoxFilePath))
		{
			LOGGER.debug("Downloading from dropBox. Path: " + taskInfoData.getCloudFileDownloadPathToDisplay());

			taskInfoData.setDownloadedFilePath(getDownloadedFilePath(download.getInputStream()));
		}
		catch (final DbxException e)
		{
			throw new CloudStorageException("An error occurred during downloading from DropBox. Path:" + filePath, e);
		}

		return taskInfoData;
	}

	@Override
	public TaskInfoData upload(final TaskInfoData taskInfoData) throws CloudStorageException
	{
		final String cloudFileUploadPath = taskInfoData.getCloudUploadFolderPath();
		final Path fileToUploadPath = taskInfoData.getFileToUploadPath();
		final DbxClientV2 client = getClient(taskInfoData.getConfig());
		final String dropBoxFilePath =
				resolveDropBoxPath(cloudFileUploadPath) + STORAGE_PATH_SEPARATOR + taskInfoData.getRealFileName();
		logUploadingFile(LOGGER, taskInfoData);

		LOGGER.info("Uploading file to Dropbox has been started...");

		File file = fileToUploadPath.toFile();
		long length = file.length();

		try (final InputStream inputStream = new FileInputStream(fileToUploadPath.toFile()))
		{

			if (length > CHUNKED_UPLOAD_CHUNK_SIZE)
			{
				dropBoxBatchUploadStrategy.uploadBatch(client, dropBoxFilePath, fileToUploadPath.toFile());
			}
			else {
				client.files().uploadBuilder(dropBoxFilePath)
						.withMode(WriteMode.ADD)
						.uploadAndFinish(inputStream);
			}

			LOGGER.info("Uploading was successful.");
		}
		catch (final DbxException e)
		{
			throw new CloudStorageException("An error occurred during uploading to DropBox.", e);
		}
		catch (final IOException e)
		{
			throw new CloudStorageException("An error occurred while operating with local file or directory.", e);
		}

		return taskInfoData;
	}

	@Override
	public Collection<CloudObjectData> listFiles(final TaskInfoData taskInfoData) throws CloudStorageException
	{
		final List<Metadata> metadataList = getMetadata(taskInfoData);

		return getCloudObjectDataList(metadataList, taskInfoData.getIsExport());
	}

	@Override
	public long getSize(final StorageConfigData storageConfig, final String location) throws CloudStorageException
	{
		final DbxClientV2 client = getClient(storageConfig);
		final String dropBoxFilePath = getDropboxConnectionProvider().getEnsuredPath(location);
		try
		{
			final Metadata metadata = client.files().getMetadata(dropBoxFilePath);
			return metadata instanceof FileMetadata
					? ((FileMetadata) metadata).getSize()
					: 0L;
		}
		catch (DbxException e)
		{
			throw new CloudStorageException("An error occurred during getting Size from DropBox", e);
		}
	}

	@Override
	public InputStream getAsStream(final StorageConfigData storageConfig, final String location) throws CloudStorageException
	{
		final DbxClientV2 client = getClient(storageConfig);
		final String dropBoxFilePath = getDropboxConnectionProvider().getEnsuredPath(location);
		try (final DbxDownloader<FileMetadata> download = client.files().download(dropBoxFilePath))
		{
			return download.getInputStream();
		}
		catch (DbxException e)
		{
			throw new CloudStorageException("An error occurred during getting Stream from DropBox", e);
		}
	}

	@Override
	public void delete(final StorageConfigData storageConfig, final String location) throws CloudStorageException
	{
		final DbxClientV2 client = getClient(storageConfig);
		final String dropBoxFilePath = getDropboxConnectionProvider().getEnsuredPath(location);
		try
		{
			client.files().deleteV2(dropBoxFilePath);

			LOGGER.debug("Removed " + location + " from " + storageConfig.getCode());
		}
		catch (DbxException e)
		{
			throw new CloudStorageException("An error occurred during removing from DropBox", e);
		}
	}

	private List<Metadata> getMetadata(final TaskInfoData taskInfoData) throws CloudStorageException
	{
		final DbxClientV2 client = getClient(taskInfoData.getConfig());

		final List<Metadata> result = new ArrayList<>();

		try
		{
			final String cloudFolderPath = taskInfoData.getCloudFolderPath();
			ListFolderResult listFolderResult = client.files().listFolder(cloudFolderPath);
			do
			{
				result.addAll(listFolderResult.getEntries());
				final String cursor = listFolderResult.getCursor();
				listFolderResult = client.files().listFolderContinue(cursor);
				LOGGER.debug("Get dropBox list folder: " + cloudFolderPath);
			}
			while (listFolderResult.getHasMore());
		}
		catch (final DbxException e)
		{
			throw new CloudStorageException("Can't get dropBox list folder", e);
		}

		return result;
	}

	private String resolveDropBoxPath(final String path) throws CloudStorageException
	{
		if (StringUtils.isBlank(path) || STORAGE_PATH_SEPARATOR.equals(path))
		{
			return ROOT;
		}

		if (!dropboxConnectionProvider.isDropBoxPathValid(path))
		{
			throw new CloudStorageException("Invalid path given");
		}

		return path;
	}

	private List<CloudObjectData> getCloudObjectDataList(final List<Metadata> metadataList, final boolean isExport)
	{
		final List<CloudObjectData> result = new ArrayList<>();

		for (final Metadata metadata : metadataList)
		{
			if (metadata instanceof FolderMetadata)
			{
				result.add(getFolderDataConverter().convert(metadata));
			}
			else if (isZipFileToImport(isExport, metadata))
			{
				result.add(getFileDataConverter().convert(metadata));
			}
		}

		return result;
	}

	private boolean isZipFileToImport(final boolean isExport, final Metadata metadata)
	{
		return !isExport && metadata instanceof FileMetadata && metadata.getName().contains(IEM_TRANSMIT_FILE_EXTENSION);
	}

	protected DbxClientV2 getClient(final StorageConfigData config) throws CloudStorageException
	{
		getStorageConfigValidator().validate(config);

		if (StringUtils.isBlank(config.getAccessToken()))
		{
			throw new CloudStorageException("Token is expired, check auth please");
		}

		return dropboxConnectionProvider.getDropBoxClient(clientIdentifier, config.getAccessToken());
	}

	public Converter<Metadata, CloudObjectData> getFileDataConverter()
	{
		return fileDataConverter;
	}

	@Required
	public void setFileDataConverter(final Converter<Metadata, CloudObjectData> fileDataConverter)
	{
		this.fileDataConverter = fileDataConverter;
	}

	public Converter<Metadata, CloudObjectData> getFolderDataConverter()
	{
		return folderDataConverter;
	}

	@Required
	public void setFolderDataConverter(final Converter<Metadata, CloudObjectData> folderDataConverter)
	{
		this.folderDataConverter = folderDataConverter;
	}

	public String getClientIdentifier()
	{
		return clientIdentifier;
	}

	@Required
	public void setClientIdentifier(final String clientIdentifier)
	{
		this.clientIdentifier = clientIdentifier;
	}

	public DropboxConnectionProvider getDropboxConnectionProvider()
	{
		return dropboxConnectionProvider;
	}

	@Required
	public void setDropboxConnectionProvider(final DropboxConnectionProvider dropboxConnectionProvider)
	{
		this.dropboxConnectionProvider = dropboxConnectionProvider;
	}

	public DropBoxBatchUploadStrategy getDropBoxBatchUploadStrategy()
	{
		return dropBoxBatchUploadStrategy;
	}

	@Required
	public void setDropBoxBatchUploadStrategy(
			DropBoxBatchUploadStrategy dropBoxBatchUploadStrategy)
	{
		this.dropBoxBatchUploadStrategy = dropBoxBatchUploadStrategy;
	}
}


