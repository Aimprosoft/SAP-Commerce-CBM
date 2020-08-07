package com.aimprosoft.importexportcloud.service.validators;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;


public interface StorageConfigValidator
{
	/**
	 * Validates storage configuration using corresponding implementation of
	 * {@link com.aimprosoft.importexportcloud.service.validators.strategies.StorageConfigValidationStrategy}.
	 *
	 * @param storageConfigData storage configuration DTO
	 * @throws CloudStorageException if storage configuration is invalid
	 */
	void validate(StorageConfigData storageConfigData) throws CloudStorageException;
}
