package com.aimprosoft.importexportcloud.converters.populators;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.user.UserService;

import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;
import com.aimprosoft.importexportcloud.model.StorageConfigModel;
import com.aimprosoft.importexportcloud.model.StorageTypeModel;
import com.aimprosoft.importexportcloud.service.StorageTypeService;


public class BaseStorageConfigReversePopulator implements Populator<StorageConfigData, StorageConfigModel>
{
	private StorageTypeService storageTypeService;

	private KeyGenerator keyGenerator;

	private UserService userService;

	@Override
	public void populate(final StorageConfigData source, final StorageConfigModel target) throws ConversionException
	{
		if (source != null)
		{
			target.setCode((String) keyGenerator.generate());
			target.setName(source.getName());

			setStorageTypeModel(source, target);

			if (source.getIsConnected() != null)
			{
				target.setIsConnected(source.getIsConnected());
			}

			target.setUser(userService.getCurrentUser());
		}
	}

	private void setStorageTypeModel(final StorageConfigData source, final StorageConfigModel target)
	{
		final StorageTypeData storageTypeData = source.getStorageTypeData();

		if (storageTypeData != null)
		{
			final String storageTypeCode = storageTypeData.getCode();

			if (storageTypeCode != null)
			{
				final StorageTypeModel storageTypeModel = storageTypeService.getStorageTypeByCode(storageTypeCode);
				target.setType(storageTypeModel);
			}
		}
	}

	public StorageTypeService getStorageTypeService()
	{
		return storageTypeService;
	}

	@Required
	public void setStorageTypeService(final StorageTypeService storageTypeService)
	{
		this.storageTypeService = storageTypeService;
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
