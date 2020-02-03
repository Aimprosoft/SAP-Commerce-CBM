package com.aimprosoft.importexportcloud.facades;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.model.StorageConfigModel;

import java.util.List;


public interface StorageConfigFacade
{
	/**
	 * Gets a list of storage config data for particular storage type and current user.
	 * If none are found an empty list is returned.
	 *
	 * @param storageTypeCode uniq storage type code
	 * @return a list o storage config data
	 */
	List<StorageConfigData> getStorageConfigsDataByTypeCode(String storageTypeCode);

	/**
	 * Gets the storage config data if exists for the given code.
	 *
	 * @param code uniq storage config code
	 * @return storage config data
	 */
	StorageConfigData getStorageConfigData(String code);

	/**
	 * Gets the storage config model if exists for the given code.
	 *
	 * @param code uniq storage config code
	 * @return storage config model
	 */
	StorageConfigModel getStorageConfigModelByCode(String code);

	/**
	 * Creates storage config model for current user with set storage type.
	 *
	 * @param storageTypeCode storage type code for storage config creation/
	 * @return storage config model
	 * @throws IllegalArgumentException if there is no such a storage config model or reverse converter for it.
	 */
	StorageConfigModel createStorageConfigModel(String storageTypeCode);

	/**
	 * Removes saved storage config.
	 *
	 * @param code code of storage config to be removed
	 */
	void removeStorageConfig(String code);
}
