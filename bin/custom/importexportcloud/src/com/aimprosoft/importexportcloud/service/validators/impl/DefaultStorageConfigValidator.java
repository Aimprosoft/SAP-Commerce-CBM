package com.aimprosoft.importexportcloud.service.validators.impl;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;
import com.aimprosoft.importexportcloud.service.validators.StorageConfigValidator;
import com.aimprosoft.importexportcloud.service.validators.strategies.StorageConfigValidationStrategy;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import java.util.Map;


public class DefaultStorageConfigValidator implements StorageConfigValidator
{
	Map<String, StorageConfigValidationStrategy> storageConfigValidationStrategyMap;

	@Override
	public void validate(final StorageConfigData storageConfigData) throws CloudStorageException
	{
		final StorageTypeData storageTypeData = storageConfigData.getStorageTypeData();

		validateStorageTypeData(storageTypeData);

		final String storageTypeCode = storageTypeData.getCode();

		final StorageConfigValidationStrategy storageConfigValidator = storageConfigValidationStrategyMap.get(storageTypeCode);

		if (storageConfigValidator != null)
		{
			storageConfigValidator.validate(storageConfigData);
		}
		else
		{
			throw new CloudStorageException(String.format("Validator for storage type %s is not found.", storageTypeCode));
		}
	}

	private void validateStorageTypeData(final StorageTypeData storageTypeData) throws CloudStorageException
	{
		if (storageTypeData == null)
		{
			throw new CloudStorageException("Storage type data in storage config is null.");
		}

		if (StringUtils.isEmpty(storageTypeData.getCode()))
		{
			throw new CloudStorageException("Storage type code is null or empty.");
		}
	}

	public Map<String, StorageConfigValidationStrategy> getStorageConfigValidationStrategyMap()
	{
		return storageConfigValidationStrategyMap;
	}

	@Required
	public void setStorageConfigValidationStrategyMap(
			final Map<String, StorageConfigValidationStrategy> storageConfigValidationStrategyMap)
	{
		this.storageConfigValidationStrategyMap = storageConfigValidationStrategyMap;
	}
}
