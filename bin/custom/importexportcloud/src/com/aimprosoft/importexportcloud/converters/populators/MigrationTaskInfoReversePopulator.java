package com.aimprosoft.importexportcloud.converters.populators;

import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.MigrationTaskInfoModel;
import com.aimprosoft.importexportcloud.model.StorageConfigModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.user.UserService;
import org.springframework.beans.factory.annotation.Required;


public class MigrationTaskInfoReversePopulator implements Populator<TaskInfoData, MigrationTaskInfoModel>
{
	private MediaService mediaService;

	private KeyGenerator keyGenerator;

	private StorageConfigFacade storageConfigFacade;

	private UserService userService;

	@Override
	public void populate(final TaskInfoData source, final MigrationTaskInfoModel target) throws ConversionException
	{
		if (source != null)
		{
			final MediaFolderModel mediaFolder = mediaService.getFolder(source.getMediaFolderQualifier());
			final StorageConfigModel sourceStorageConfig = storageConfigFacade
					.getStorageConfigModelByCode(source.getSourceConfigCode());
			final StorageConfigData targetConfigData = source.getConfig();

			target.setSourceStorageConfig(sourceStorageConfig);
			target.setMediaFolder(mediaFolder);
			if (targetConfigData != null)
			{
				final StorageConfigModel targetStorageConfig = storageConfigFacade
						.getStorageConfigModelByCode(targetConfigData.getCode());
				target.setStorageConfig(targetStorageConfig);
			}

			final String generatedCode = (String) keyGenerator.generate();

			target.setCode(generatedCode);

			target.setUser(userService.getCurrentUser());
		}
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


	public KeyGenerator getKeyGenerator()
	{
		return keyGenerator;
	}

	@Required
	public void setKeyGenerator(final KeyGenerator keyGenerator)
	{
		this.keyGenerator = keyGenerator;
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

	public UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}
}
