package com.aimprosoft.importexportcloud.strategies;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;
import com.aimprosoft.importexportcloud.service.connection.ConnectionService;
import com.aimprosoft.importexportcloud.service.storage.StorageService;
import org.springframework.beans.factory.annotation.Required;

import java.util.Map;
import java.util.Optional;


public class IemServiceTypeLocator implements ServiceTypeLocator
{
	private Map<String, ConnectionService> connectionServicesMap;

	private Map<String, StorageService> storageServicesMap;

	@Override
	public ConnectionService getConnectionService(final StorageConfigData storageConfigData)
	{
		return Optional.ofNullable(storageConfigData)
				.map(StorageConfigData::getStorageTypeData)
				.map(StorageTypeData::getCode)
				.map(connectionServicesMap::get)
				.orElse(null);
	}

	@Override
	public StorageService getStorageService(final StorageConfigData storageConfigData)
	{
		return Optional.ofNullable(storageConfigData)
				.map(StorageConfigData::getStorageTypeData)
				.map(StorageTypeData::getCode)
				.map(storageServicesMap::get)
				.orElse(null);
	}

	@Required
	public void setConnectionServicesMap(
			final Map<String, ConnectionService> connectionServicesMap)
	{
		this.connectionServicesMap = connectionServicesMap;
	}

	@Required
	public void setStorageServicesMap(
			final Map<String, StorageService> storageServicesMap)
	{
		this.storageServicesMap = storageServicesMap;
	}
}
