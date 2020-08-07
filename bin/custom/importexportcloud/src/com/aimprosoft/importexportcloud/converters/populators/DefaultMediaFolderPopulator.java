package com.aimprosoft.importexportcloud.converters.populators;

import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.data.MediaFolderData;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.model.StorageConfigModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.media.storage.MediaStorageConfigService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.beans.factory.annotation.Required;

import static com.aimprosoft.importexportcloud.constants.ImportexportcloudConstants.LOCAL_STORAGE_CONFIG;
import static com.aimprosoft.importexportcloud.constants.ImportexportcloudConstants.LOCAL_STORAGE_TYPE_NAME;


public class DefaultMediaFolderPopulator implements Populator<MediaFolderModel, MediaFolderData>
{
	private MediaStorageConfigService mediaStorageConfigService;

	private StorageConfigFacade storageConfigFacade;

	@Override
	public void populate(final MediaFolderModel mediaFolderModel, final MediaFolderData mediaFolderData) throws ConversionException
	{
		mediaFolderData.setQualifier(mediaFolderModel.getQualifier());

		mediaFolderData.setCanMigrate(mediaFolderModel.isCanMigrate());

		final StorageConfigModel storageConfigModel = mediaFolderModel.getCurrentStorageConfig();
		if (storageConfigModel != null)
		{
			mediaFolderData.setStorageConfigCode(storageConfigModel.getCode());
			mediaFolderData.setStorageConfigName(storageConfigModel.getName());
			mediaFolderData.setStorageTypeName(storageConfigModel.getType().getName());
		}
		else
		{
			//in case of OOTB strategy for this folder
			final MediaStorageConfigService.MediaFolderConfig configForFolder = mediaStorageConfigService
					.getConfigForFolder(mediaFolderData.getQualifier());

			final String storageStrategyId = configForFolder.getStorageStrategyId();
			final String storageConfigName = getConfigName(storageStrategyId);

			mediaFolderData.setStorageConfigCode(LOCAL_STORAGE_CONFIG);
			mediaFolderData.setStorageConfigName(storageConfigName);
			mediaFolderData.setStorageTypeName(LOCAL_STORAGE_TYPE_NAME);
		}
	}

	private String getConfigName(final String storageStrategyId)
	{
		String configName;

		if ("localFileMediaStorageStrategy".equals(storageStrategyId))
		{
			final StorageConfigData storageConfigData = storageConfigFacade.getStorageConfigData(LOCAL_STORAGE_CONFIG);
			configName = storageConfigData.getName();
		}
		else
		{
			configName = storageStrategyId;
		}
		return configName;
	}

	public MediaStorageConfigService getMediaStorageConfigService()
	{
		return mediaStorageConfigService;
	}

	@Required
	public void setMediaStorageConfigService(final MediaStorageConfigService mediaStorageConfigService)
	{
		this.mediaStorageConfigService = mediaStorageConfigService;
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


}
