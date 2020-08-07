package com.aimprosoft.importexportcloud.strategies.storage;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.MigrationTaskInfoModel;
import com.aimprosoft.importexportcloud.service.storage.StorageService;
import com.aimprosoft.importexportcloud.strategies.IemServiceTypeLocator;
import com.google.common.base.Preconditions;
import de.hybris.platform.core.Registry;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.media.exceptions.MediaNotFoundException;
import de.hybris.platform.media.services.MediaLocationHashService;
import de.hybris.platform.media.services.impl.HierarchicalMediaPathBuilder;
import de.hybris.platform.media.storage.MediaMetaData;
import de.hybris.platform.media.storage.MediaStorageConfigService;
import de.hybris.platform.media.storage.MediaStorageRegistry;
import de.hybris.platform.media.storage.MediaStorageStrategy;
import de.hybris.platform.media.storage.impl.StoredMediaData;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.util.MediaUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.Map;
import java.util.Optional;


public class ConfigurableMediaStorageStrategy implements MediaStorageStrategy
{
	private static final Logger LOG = LoggerFactory.getLogger(ConfigurableMediaStorageStrategy.class);
	private static final String DEFAULT_MIME = "image/jpeg";
	private static final String TENANT_PREFIX = "sys-";
	private static final String FILE_SEPARATOR = "/";

	private MediaLocationHashService mediaLocationHashService;

	private StorageConfigFacade storageConfigFacade;

	private IemServiceTypeLocator iemServiceTypeLocator;

	private MediaService mediaService;

	private MediaStorageRegistry mediaStorageRegistry;

	@Override
	public StoredMediaData store(final MediaStorageConfigService.MediaFolderConfig mediaFolderConfig, final String mediaId,
			final Map<String, Object> metaData, final InputStream inputStream)
	{
		Preconditions.checkArgument(mediaFolderConfig != null, "config is required");
		Preconditions.checkArgument(mediaId != null, "mediaId is required");
		Preconditions.checkArgument(metaData != null, "metaData cannot be nullable");
		Preconditions.checkArgument(inputStream != null, "data input stream is required");

		final StorageConfigData storageConfig = storageConfigFacade
				.getStorageConfigForFolder(mediaFolderConfig.getFolderQualifier(), true);

		final StorageService cloudStorageService = iemServiceTypeLocator.getStorageService(storageConfig);

		if (cloudStorageService == null || storageConfig.getStorageTypeData().getIsLocal())
		{
			return getLocalStoreMediaData(mediaFolderConfig, mediaId, metaData, inputStream);
		}

		return getStorageMediaData(mediaFolderConfig, storageConfig, metaData, inputStream, cloudStorageService, mediaId);
	}

	private StoredMediaData getStorageMediaData(final MediaStorageConfigService.MediaFolderConfig mediaFolderConfig,
			final StorageConfigData storageConfig, final Map<String, Object> metaData, final InputStream inputStream,
			final StorageService cloudStorageService, final String mediaId)
	{
		final String realFileName = (String) metaData.get(MediaMetaData.FILE_NAME);

		final String location = assembleLocation(mediaFolderConfig, mediaId, realFileName, storageConfig);

		TaskInfoData taskInfoData = getTaskData(mediaFolderConfig, storageConfig, inputStream, metaData, mediaId, location);

		try
		{
			taskInfoData = cloudStorageService.upload(taskInfoData);

			return createStorageMediaData(mediaFolderConfig, taskInfoData, metaData, location);
		}
		catch (CloudStorageException e)
		{
			LOG.warn("Error during media storing", e);
		}
		finally
		{
			try
			{
				if (taskInfoData != null)
				{
					Files.delete(taskInfoData.getFileToUploadPath());
				}
			}
			catch (IOException e)
			{
				LOG.warn(String.format("File %s was not removed. ", taskInfoData.getFileToUploadPath()), e);
			}
		}

		return null;
	}

	private StoredMediaData getLocalStoreMediaData(final MediaStorageConfigService.MediaFolderConfig mediaFolderConfig,
			final String mediaId, final Map<String, Object> metaData, final InputStream inputStream)
	{
		final MediaStorageStrategy storageStrategyForFolder = mediaStorageRegistry.getStorageStrategyForFolder(mediaFolderConfig);

		return storageStrategyForFolder.store(mediaFolderConfig, mediaId, metaData, inputStream);

	}


	private StoredMediaData createStorageMediaData(final MediaStorageConfigService.MediaFolderConfig mediaFolderConfig,
			final TaskInfoData taskInfoData, final Map<String, Object> metaData, final String location)
	{
		long size = 0;

		if (taskInfoData.getFileToUploadPath() != null)
		{
			size = taskInfoData.getFileToUploadPath().toFile().length();
		}

		final String hash = mediaLocationHashService.createHashForLocation(mediaFolderConfig.getFolderQualifier(), location);

		final String imageJpegMime = getImageJpegMime(metaData);

		return new StoredMediaData(location, hash, size, imageJpegMime);
	}

	@Override
	public long getSize(final MediaStorageConfigService.MediaFolderConfig mediaFolderConfig, final String location)
	{
		final StorageConfigData storageConfig = obtainStorageConfigFromLocation(location);
		final StorageService storageService = iemServiceTypeLocator.getStorageService(storageConfig);

		if (storageService == null)
		{
			final MediaStorageStrategy storageStrategyForFolder = mediaStorageRegistry
					.getStorageStrategyForFolder(mediaFolderConfig);

			return storageStrategyForFolder.getSize(mediaFolderConfig, location);
		}
		else
		{
			try
			{
				return storageService.getSize(storageConfig, location);
			}
			catch (CloudStorageException e)
			{
				LOG.warn("Could not compute media size, location={}", location);
			}
		}

		return 0L;
	}

	@Override
	public void delete(final MediaStorageConfigService.MediaFolderConfig mediaFolderConfig, final String location)
	{
		final StorageConfigData storageConfig = obtainStorageConfigFromLocation(location);

		final StorageService storageService = iemServiceTypeLocator.getStorageService(storageConfig);

		if (storageService == null)
		{
			final MediaStorageStrategy storageStrategyForFolder = mediaStorageRegistry
					.getStorageStrategyForFolder(mediaFolderConfig);

			storageStrategyForFolder.delete(mediaFolderConfig, location);
		}
		else
		{
			try
			{
				storageService.delete(storageConfig, location);
			}
			catch (CloudStorageException e)
			{
				LOG.warn("Cannot delete file location={}, storage={}", location, storageConfig.getCode());
			}
		}
	}

	@Override
	public InputStream getAsStream(final MediaStorageConfigService.MediaFolderConfig mediaFolderConfig, final String location)
	{
		final StorageConfigData storageConfig = obtainStorageConfigFromLocation(location);
		final StorageService storageService = iemServiceTypeLocator.getStorageService(storageConfig);

		if (storageService == null)
		{
			final MediaStorageStrategy storageStrategyForFolder = mediaStorageRegistry
					.getStorageStrategyForFolder(mediaFolderConfig);

			return storageStrategyForFolder.getAsStream(mediaFolderConfig, location);
		}
		else
		{
			try
			{
				return storageService.getAsStream(storageConfig, location);
			}
			catch (CloudStorageException e)
			{
				throw new MediaNotFoundException("Media not found (requested media location: " + location + ")", e);
			}
		}
	}

	@Override
	public File getAsFile(final MediaStorageConfigService.MediaFolderConfig mediaFolderConfig, final String location)
	{
		throw new UnsupportedOperationException("Obtaining media as file is not supported. Use getMediaAsStream method.");
	}

	private StorageConfigData obtainStorageConfigFromLocation(final String location)
	{
		final String storageCode = StringUtils.substringBetween(location, "storageCode=", "/");

		if (storageCode == null)
		{
			return null;
		}

		return storageConfigFacade.getStorageConfigData(storageCode);
	}


	private TaskInfoData getTaskData(final MediaStorageConfigService.MediaFolderConfig mediaFolderConfig,
			final StorageConfigData storageConfig, final InputStream streamToUpload, final Map<String, Object> metaData,
			final String mediaId, final String location)
	{
		final TaskInfoData taskInfoData = new TaskInfoData();
		taskInfoData.setConfig(storageConfig);
		taskInfoData.setTaskInfoCode(mediaId);

		taskInfoData.setRealFileName(location);
		//root folder
		taskInfoData.setCloudUploadFolderPath("");

		taskInfoData.setMediaFolderQualifier(mediaFolderConfig.getFolderQualifier());

		final String mediaMimeType = (String) metaData.get("mime");

		taskInfoData.setMigrationMediaMimeType(mediaMimeType);

		if (MapUtils.isNotEmpty(metaData))
		{
			taskInfoData.setResultSize((Long) metaData.get(MediaMetaData.SIZE));
		}
		try
		{
			final Path pathToUpload = File.createTempFile(mediaId, ".migration").toPath();
			Files.copy(streamToUpload, pathToUpload, StandardCopyOption.REPLACE_EXISTING);
			taskInfoData.setFileToUploadPath(pathToUpload);

			return taskInfoData;
		}
		catch (IOException e)
		{
			LOG.error("Can not create storage dto from the passed params", e);
		}

		return null;
	}

	private String getImageJpegMime(final Map<String, Object> metaData)
	{
		final String metaDataMime = (String) metaData.get(MediaMetaData.MIME);
		return Optional.ofNullable(metaDataMime).orElse(DEFAULT_MIME);
	}

	private String getTenantPrefix()
	{
		return TENANT_PREFIX + Registry.getCurrentTenantNoFallback().getTenantID().toLowerCase();
	}

	private String assembleLocation(final MediaStorageConfigService.MediaFolderConfig config, final String mediaId,
			final String realFileName, final StorageConfigData configData)
	{
		final StringBuilder builder = new StringBuilder(MediaUtil.addTrailingFileSepIfNeeded(getTenantPrefix()));
		final HierarchicalMediaPathBuilder pathBuilder = HierarchicalMediaPathBuilder.forDepth(config.getHashingDepth());

		final StringBuilder folderQualifierAndStorage = new StringBuilder(configData.getStorageTypeData().getIsLocal()
				? config.getFolderQualifier()
				: config.getFolderQualifier() + FILE_SEPARATOR + "storageCode=" + configData.getCode());

		final MediaFolderModel mediaFolder = mediaService.getFolder(config.getFolderQualifier());
		final MigrationTaskInfoModel migrationTaskInfo = mediaFolder.getLastMigrationTaskInfo();
		if (migrationTaskInfo != null)
		{
			final Date creationTime = migrationTaskInfo.getCreationtime();
			folderQualifierAndStorage.append(FILE_SEPARATOR);
			folderQualifierAndStorage.append(creationTime.getTime());
			folderQualifierAndStorage.append(FILE_SEPARATOR);
		}
		else
		{
			LOG.warn("migrationTask is null. Media will be stored without a folder time indicator");
		}
		builder.append(pathBuilder.buildPath(folderQualifierAndStorage.toString(), mediaId));
		builder.append(mediaId);

		if (StringUtils.isNotBlank(realFileName))
		{
			builder.append(MediaUtil.addLeadingFileSepIfNeeded(realFileName));
		}
		return builder.toString();
	}

	public MediaLocationHashService getMediaLocationHashService()
	{
		return mediaLocationHashService;
	}

	@Required
	public void setMediaLocationHashService(final MediaLocationHashService mediaLocationHashService)
	{
		this.mediaLocationHashService = mediaLocationHashService;
	}

	public StorageConfigFacade getStorageConfigFacade()
	{
		return storageConfigFacade;
	}

	@Required
	public void setStorageConfigFacade(final StorageConfigFacade storageConfigFacade)
	{
		this.storageConfigFacade = storageConfigFacade;
	}

	public IemServiceTypeLocator getIemServiceTypeLocator()
	{
		return iemServiceTypeLocator;
	}

	@Required
	public void setIemServiceTypeLocator(final IemServiceTypeLocator iemServiceTypeLocator)
	{
		this.iemServiceTypeLocator = iemServiceTypeLocator;
	}

	public MediaService getMediaService()
	{
		return mediaService;
	}

	@Required
	public void setMediaService(final MediaService mediaService)
	{
		this.mediaService = mediaService;
	}

	public MediaStorageRegistry getMediaStorageRegistry()
	{
		return mediaStorageRegistry;
	}

	@Required
	public void setMediaStorageRegistry(final MediaStorageRegistry mediaStorageRegistry)
	{
		this.mediaStorageRegistry = mediaStorageRegistry;
	}
}
