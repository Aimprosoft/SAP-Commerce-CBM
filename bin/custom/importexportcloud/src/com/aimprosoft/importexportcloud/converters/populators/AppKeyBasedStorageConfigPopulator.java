package com.aimprosoft.importexportcloud.converters.populators;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.model.AppKeyBasedStorageConfigModel;


public class AppKeyBasedStorageConfigPopulator implements Populator<AppKeyBasedStorageConfigModel, StorageConfigData>
{
	@Override
	public void populate(final AppKeyBasedStorageConfigModel source, final StorageConfigData target) throws ConversionException
	{
		if (source != null)
		{
			target.setAppKey(source.getAppKey());
			target.setEncodedAppSecret(source.getEncodedAppSecret());
			target.setEnableSavingUrls(source.getEnableSavingUrls());
		}
	}
}
