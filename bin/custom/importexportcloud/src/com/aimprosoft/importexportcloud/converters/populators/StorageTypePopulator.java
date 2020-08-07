package com.aimprosoft.importexportcloud.converters.populators;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;
import com.aimprosoft.importexportcloud.model.StorageTypeModel;


public class StorageTypePopulator implements Populator<StorageTypeModel, StorageTypeData>
{
	@Override
	public void populate(final StorageTypeModel source, final StorageTypeData target) throws ConversionException
	{
		if (source != null)
		{
			target.setCode(source.getCode());
			target.setName(source.getName());
			target.setIsLocal(source.getLocal());
			target.setIsDefault(source.getDefault());
			target.setIsAuthNeeded(source.getAuthNeeded());
		}
	}
}
