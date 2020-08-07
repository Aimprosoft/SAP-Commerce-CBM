package com.aimprosoft.importexportcloud.service.impl;

import com.aimprosoft.importexportcloud.model.StorageTypeModel;
import com.aimprosoft.importexportcloud.service.StorageTypeService;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;
import org.springframework.beans.factory.annotation.Required;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateIfSingleResult;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static java.lang.String.format;


public class DefaultStorageTypeService implements StorageTypeService
{
	private GenericDao<StorageTypeModel> storageTypeDao;

	@Override
	public List<StorageTypeModel> getAllStorageTypes()
	{
		return storageTypeDao.find();
	}

	@Override
	public StorageTypeModel getStorageTypeByCode(final String code)
	{
		validateParameterNotNullStandardMessage(StorageTypeModel.CODE, code);

		final Map<String, Object> searchParameterMap = new HashMap<>();
		searchParameterMap.put(StorageTypeModel.CODE, code);
		final List<StorageTypeModel> storageTypeList = storageTypeDao.find(searchParameterMap);

		validateIfSingleResult(storageTypeList, format("StorageType with code '%s' not found!", code),
				format("StorageType code '%s' is not unique, %d storage types found!", code, storageTypeList.size()));

		return storageTypeList.get(0);
	}

	public GenericDao<StorageTypeModel> getStorageTypeDao()
	{
		return storageTypeDao;
	}

	@Required
	public void setStorageTypeDao(final GenericDao<StorageTypeModel> storageTypeDao)
	{
		this.storageTypeDao = storageTypeDao;
	}
}
