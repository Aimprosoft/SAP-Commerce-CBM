package com.aimprosoft.importexportcloud.facades.impl;

import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.facades.StorageTypeFacade;
import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;
import com.aimprosoft.importexportcloud.model.StorageTypeModel;
import com.aimprosoft.importexportcloud.service.StorageTypeService;


public class DefaultStorageTypeFacade implements StorageTypeFacade
{
	private StorageTypeService storageTypeService;
	private Converter<StorageTypeModel, StorageTypeData> storageTypeConverter;

	@Override
	public List<StorageTypeData> getStorageTypesData()
	{
		final List<StorageTypeModel> storageTypeList = storageTypeService.getAllStorageTypes();
		return storageTypeConverter.convertAll(storageTypeList);
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

	@Required
	public void setStorageTypeConverter(
			final Converter<StorageTypeModel, StorageTypeData> storageTypeConverter)
	{
		this.storageTypeConverter = storageTypeConverter;
	}
}
