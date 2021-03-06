package com.aimprosoft.importexportcloud.service.storage.impl;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.data.CloudObjectData;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.providers.S3ConnectionProvider;
import com.aimprosoft.importexportcloud.service.storage.StorageService;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.aimprosoft.importexportcloud.constants.ImportexportcloudConstants.IEM_TRANSMIT_FILE_EXTENSION;
import static com.aimprosoft.importexportcloud.constants.ImportexportcloudConstants.STORAGE_PATH_SEPARATOR;


/**
 * S3StorageService implementation of {@link StorageService}.
 *
 * @version S3StorageService v1.0
 */
public class S3StorageService extends AbstractStorageService
{
	private static final Logger LOGGER = Logger.getLogger(S3StorageService.class);

	private Converter<S3Object, CloudObjectData> s3FileDataConverter;

	private Converter<CommonPrefix, CloudObjectData> s3FolderDataConverter;

	private S3ConnectionProvider s3ConnectionProvider;

	@Override
	public TaskInfoData download(final TaskInfoData taskInfoData) throws CloudStorageException
	{
		final S3Client s3Client = getS3Client(taskInfoData.getConfig());

		final String bucketName = taskInfoData.getConfig().getBucketName();
		final String cloudFileDownloadPath = taskInfoData.getCloudFileDownloadPath();

		final InputStream cloudFileInputStream;

		try
		{
			LOGGER.debug("Downloading from Amazon S3. Path: " + taskInfoData.getCloudFileDownloadPathToDisplay());

			cloudFileInputStream = s3Client
					.getObject(GetObjectRequest.builder().bucket(bucketName).key(cloudFileDownloadPath).build());
		}
		catch (final SdkException e)
		{
			throw new CloudStorageException("An error occurred during downloading from Amazon S3.", e);
		}

		final Path downloadedFilePath = getDownloadedFilePath(cloudFileInputStream);
		taskInfoData.setDownloadedFilePath(downloadedFilePath);

		return taskInfoData;
	}

	@Override
	public TaskInfoData upload(final TaskInfoData taskInfoData) throws CloudStorageException
	{
		final S3Client s3Client = getS3Client(taskInfoData.getConfig());
		final String cloudUploadFolderPath = taskInfoData.getCloudUploadFolderPath();
		final String cloudStoragePath = cloudUploadFolderPath.equals(STORAGE_PATH_SEPARATOR) ? StringUtils.EMPTY : cloudUploadFolderPath;

		final String key = cloudStoragePath + taskInfoData.getRealFileName();
		final Path fileToUploadPath = taskInfoData.getFileToUploadPath();
		final String bucketName = taskInfoData.getConfig().getBucketName();
		logUploadingFile(LOGGER, taskInfoData);
		try
		{
			LOGGER.debug("Starting uploading file to Amazon S3...");

			s3Client.putObject(
					PutObjectRequest.builder()
							.bucket(bucketName)
							.key(key)
							.contentType(taskInfoData.getMigrationMediaMimeType())
							.contentLength(fileToUploadPath.toFile().length())
							.build(),
					RequestBody.fromFile(fileToUploadPath));

			LOGGER.debug("Uploading was successful.");
		}
		catch (final SdkException e)
		{
			throw new CloudStorageException("An error occurred during uploading to Amazon S3.", e);
		}

		return taskInfoData;
	}

	@Override
	public Collection<CloudObjectData> listFiles(final TaskInfoData taskInfoData) throws CloudStorageException
	{
		final S3Client s3Client = getS3Client(taskInfoData.getConfig());

		final String bucketName = taskInfoData.getConfig().getBucketName();
		final String prefix = taskInfoData.getCloudFolderPath();

		final ListObjectsResponse listObjectsResponse;

		try
		{
			listObjectsResponse = s3Client.listObjects(
					ListObjectsRequest.builder().bucket(bucketName).prefix(prefix).delimiter(STORAGE_PATH_SEPARATOR).build());
		}
		catch (final SdkException e)
		{
			throw new CloudStorageException("An error occurred during getting list of objects from Amazon S3.", e);
		}

		final List<CloudObjectData> result = new ArrayList<>(getFolderCloudObjects(listObjectsResponse));

		if (!taskInfoData.getIsExport())
		{
			result.addAll(getZipFileCloudObjects(listObjectsResponse));
		}

		return result;
	}

	@Override
	public InputStream getAsStream(final StorageConfigData storageConfig, final String location) throws CloudStorageException
	{
		final S3Client s3Client = getS3Client(storageConfig);
		final String bucket = storageConfig.getBucketName();
		try
		{
			return s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(location).build());
		}
		catch (SdkException e)
		{
			throw new CloudStorageException("An error occurred during getting a Stream from Amazon S3", e);
		}

	}

	@Override
	public void delete(final StorageConfigData storageConfig, final String location) throws CloudStorageException
	{
		final S3Client s3Client = getS3Client(storageConfig);
		final String bucket = storageConfig.getBucketName();
		try
		{
			DeleteObjectRequest request = DeleteObjectRequest.builder().bucket(bucket).key(location).build();
			s3Client.deleteObject(request);

			LOGGER.debug("Removed " + location + " from " + storageConfig.getCode());
		}
		catch (SdkException e)
		{
			throw new CloudStorageException("An error occurred during removing from Amazon S3", e);
		}
	}

	@Override
	public long getSize(final StorageConfigData storageConfig, final String location) throws CloudStorageException
	{
		final S3Client s3Client = getS3Client(storageConfig);
		return getObjectResponse(s3Client, storageConfig, location).contentLength();
	}

	private GetObjectResponse getObjectResponse(final S3Client s3Client, final StorageConfigData storageConfig, final String location) throws CloudStorageException
	{
		final String bucket = storageConfig.getBucketName();
		try
		{
			final GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(location).build();
			return s3Client.getObject(getObjectRequest).response();
		}
		catch (SdkException e)
		{
			throw new CloudStorageException("An error occurred during getting ObjectResponse from Amazon S3", e);
		}
	}

	protected S3Client getS3Client(final StorageConfigData storageConfigData) throws CloudStorageException
	{
		getStorageConfigValidator().validate(storageConfigData);

		return s3ConnectionProvider.getS3Client(storageConfigData);
	}

	private List<CloudObjectData> getFolderCloudObjects(final ListObjectsResponse listObjectsResponse)
	{
		final List<CommonPrefix> commonPrefixes = listObjectsResponse.commonPrefixes();
		return s3FolderDataConverter.convertAll(commonPrefixes);
	}

	private List<CloudObjectData> getZipFileCloudObjects(final ListObjectsResponse listObjectsResponse)
	{
		return listObjectsResponse.contents().stream()
				.filter(s3Object -> s3Object.key().contains(IEM_TRANSMIT_FILE_EXTENSION)).map(s3FileDataConverter::convert)
				.collect(Collectors.toList());
	}

	public Converter<S3Object, CloudObjectData> getS3FileDataConverter()
	{
		return s3FileDataConverter;
	}

	@Required
	public void setS3FileDataConverter(final Converter<S3Object, CloudObjectData> s3FileDataConverter)
	{
		this.s3FileDataConverter = s3FileDataConverter;
	}

	public Converter<CommonPrefix, CloudObjectData> getS3FolderDataConverter()
	{
		return s3FolderDataConverter;
	}

	@Required
	public void setS3FolderDataConverter(final Converter<CommonPrefix, CloudObjectData> s3FolderDataConverter)
	{
		this.s3FolderDataConverter = s3FolderDataConverter;
	}

	public S3ConnectionProvider getS3ConnectionProvider()
	{
		return s3ConnectionProvider;
	}

	@Required
	public void setS3ConnectionProvider(final S3ConnectionProvider s3ConnectionProvider)
	{
		this.s3ConnectionProvider = s3ConnectionProvider;
	}
}
