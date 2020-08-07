package com.aimprosoft.importexportcloud.converters.populators;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.user.UserService;

import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.StorageConfigModel;
import com.aimprosoft.importexportcloud.model.TaskInfoModel;
import com.aimprosoft.importexportcloud.service.StorageConfigService;


public class TaskInfoReversePopulator implements Populator<TaskInfoData, TaskInfoModel>
{
	private StorageConfigService storageConfigService;

	private UserService userService;

	private KeyGenerator keyGenerator;

	@Override
	public void populate(final TaskInfoData taskInfoData, final TaskInfoModel taskInfoModel) throws ConversionException
	{
		if (taskInfoData != null)
		{
			final StorageConfigData storageConfigData = taskInfoData.getConfig();
			final String storageConfigCode = storageConfigData.getCode();
			final StorageConfigModel storageConfigModel = storageConfigService.getStorageConfigByCode(storageConfigCode);

			taskInfoModel.setCode((String) keyGenerator.generate());
			taskInfoModel.setUser(userService.getCurrentUser());
			taskInfoModel.setStorageConfig(storageConfigModel);
			taskInfoModel.setExportMediaNeeded(taskInfoData.isExportMediaNeeded());
		}
	}

	public StorageConfigService getStorageConfigService()
	{
		return storageConfigService;
	}

	@Required
	public void setStorageConfigService(final StorageConfigService storageConfigService)
	{
		this.storageConfigService = storageConfigService;
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

	public KeyGenerator getKeyGenerator()
	{
		return keyGenerator;
	}

	@Required
	public void setKeyGenerator(final KeyGenerator keyGenerator)
	{
		this.keyGenerator = keyGenerator;
	}
}
