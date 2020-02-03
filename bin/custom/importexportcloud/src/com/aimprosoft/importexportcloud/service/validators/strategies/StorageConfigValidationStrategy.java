package com.aimprosoft.importexportcloud.service.validators.strategies;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;


public interface StorageConfigValidationStrategy
{
	/**
	 * Validates storage configuration for particular storage type.
	 *
	 * @param storageConfigData storage configuration DTO
	 * @throws CloudStorageException if storage configuration is invalid
	 */
	void validate(StorageConfigData storageConfigData) throws CloudStorageException;
}
