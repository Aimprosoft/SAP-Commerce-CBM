package com.aimprosoft.importexportcloud.service.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateIfSingleResult;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static java.lang.String.format;

import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;
import de.hybris.platform.servicelayer.internal.service.AbstractBusinessService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.model.DropBoxStorageConfigModel;
import com.aimprosoft.importexportcloud.model.StorageConfigModel;
import com.aimprosoft.importexportcloud.model.StorageTypeModel;
import com.aimprosoft.importexportcloud.service.StorageConfigService;
import com.aimprosoft.importexportcloud.service.StorageTypeService;


public class DefaultStorageConfigService extends AbstractBusinessService implements StorageConfigService
{
	private GenericDao<StorageConfigModel> storageConfigDao;

	private StorageTypeService storageTypeService;

	@Override
	public List<StorageConfigModel> getAllStorageConfigsByUserAndTypeCode(final UserModel user, final String storageTypeCode)
	{
		validateParameterNotNullStandardMessage(StorageConfigModel.USER, user);
		validateParameterNotNullStandardMessage(StorageConfigModel.CODE, storageTypeCode);
		final StorageTypeModel storageType = storageTypeService.getStorageTypeByCode(storageTypeCode);

		final Map<String, Object> searchParameterMap = new HashMap<>();
		searchParameterMap.put(StorageConfigModel.USER, user);
		searchParameterMap.put(StorageConfigModel.TYPE, storageType);

		return storageConfigDao.find(searchParameterMap);
	}

	@Override
	public StorageConfigModel getStorageConfigByCode(final String code)
	{
		validateParameterNotNullStandardMessage(StorageConfigModel.CODE, code);

		final Map<String, Object> searchParameterMap = new HashMap<>();
		searchParameterMap.put(StorageConfigModel.CODE, code);
		final List<StorageConfigModel> storageConfigList = storageConfigDao.find(searchParameterMap);

		validateIfSingleResult(storageConfigList, format("StorageConfig with code '%s' not found!", code),
				format("StorageConfig code '%s' is not unique, %d configs found!", code, storageConfigList.size()));

		return storageConfigList.get(0);
	}

	@Override
	public void setConnectionStatus(final String storageConfigCode, final Boolean isConnected)
	{
		final StorageConfigModel storageConfigModel = getStorageConfigByCode(storageConfigCode);
		storageConfigModel.setIsConnected(isConnected);
		getModelService().save(storageConfigModel);
	}

	@Override
	public void setDropBoxConnectionStatus(final String dropBoxConfigCode, final String accessToken, final Boolean isConnected)
	{
		final DropBoxStorageConfigModel storageConfig = (DropBoxStorageConfigModel) getStorageConfigByCode(dropBoxConfigCode);
		storageConfig.setAccessToken(accessToken);
		storageConfig.setIsConnected(isConnected);
		getModelService().save(storageConfig);
	}

	public GenericDao<StorageConfigModel> getStorageConfigDao()
	{
		return storageConfigDao;
	}

	@Required
	public void setStorageConfigDao(final GenericDao<StorageConfigModel> storageConfigDao)
	{
		this.storageConfigDao = storageConfigDao;
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
}
