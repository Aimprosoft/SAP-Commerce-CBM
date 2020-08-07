package com.aimprosoft.importexportcloud.converters.populators;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;
import com.aimprosoft.importexportcloud.model.StorageConfigModel;
import com.aimprosoft.importexportcloud.model.StorageTypeModel;


public class StorageConfigPopulator implements Populator<StorageConfigModel, StorageConfigData>
{
	private Converter<StorageTypeModel, StorageTypeData> storageTypeConverter;

	@Override
	public void populate(final StorageConfigModel source, final StorageConfigData target) throws ConversionException
	{
		if (source != null)
		{
			target.setCode(source.getCode());
			target.setName(source.getName());
			target.setStorageTypeData(storageTypeConverter.convert(source.getType()));
			target.setIsConnected(source.getIsConnected());
			target.setUseSignedUrls(source.isUseSignedURLs());
		}
	}

	public Converter<StorageTypeModel, StorageTypeData> getStorageTypeConverter()
	{
		return storageTypeConverter;
	}

	@Required
	public void setStorageTypeConverter(
			final Converter<StorageTypeModel, StorageTypeData> storageTypeConverter)
	{
		this.storageTypeConverter = storageTypeConverter;
	}
}
