package com.aimprosoft.importexportcloud.converters.populators;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.model.AWSs3StorageConfigModel;


public class AWSs3StorageConfigReversePopulator implements Populator<StorageConfigData, AWSs3StorageConfigModel>
{
	@Override
	public void populate(final StorageConfigData source, final AWSs3StorageConfigModel target) throws ConversionException
	{
		if (source != null)
		{
			target.setAppKey(source.getAppKey());
			target.setRegion(source.getRegion());
			target.setEncodedAppSecret(source.getEncodedAppSecret());
			target.setBucketName(source.getBucketName());
		}
	}
}
