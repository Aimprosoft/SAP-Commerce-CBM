package com.aimprosoft.importexportcloud.converters.populators;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.model.DropBoxStorageConfigModel;


public class DropBoxStorageConfigReversePopulator implements Populator<StorageConfigData, DropBoxStorageConfigModel>
{
	@Override
	public void populate(final StorageConfigData source, final DropBoxStorageConfigModel target) throws ConversionException
	{
		if (source != null)
		{
			target.setAppKey(source.getAppKey());
			target.setEncodedAppSecret(source.getEncodedAppSecret());
			target.setAccessToken(source.getAccessToken());
		}
	}
}
