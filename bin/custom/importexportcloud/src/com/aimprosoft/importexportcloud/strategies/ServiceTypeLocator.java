package com.aimprosoft.importexportcloud.strategies;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.service.connection.ConnectionService;
import com.aimprosoft.importexportcloud.service.storage.StorageService;


public interface ServiceTypeLocator
{
	ConnectionService getConnectionService(StorageConfigData storageConfigData);

	StorageService getStorageService(StorageConfigData storageConfigData);
}
