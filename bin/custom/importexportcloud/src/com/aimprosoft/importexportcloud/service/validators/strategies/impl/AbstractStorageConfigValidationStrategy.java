package com.aimprosoft.importexportcloud.service.validators.strategies.impl;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.service.validators.strategies.StorageConfigValidationStrategy;
import org.apache.commons.lang.StringUtils;


public abstract class AbstractStorageConfigValidationStrategy implements StorageConfigValidationStrategy
{
	@Override
	public void validate(final StorageConfigData storageConfigData) throws CloudStorageException
	{
		if (null == storageConfigData)
		{
			throw new CloudStorageException("Storage config is null.");
		}

		if (StringUtils.isBlank(storageConfigData.getAppKey()))
		{
			throw new CloudStorageException("App Key is empty.");
		}

		if (StringUtils.isBlank(storageConfigData.getEncodedAppSecret()))
		{
			throw new CloudStorageException("App secret is empty/");
		}
	}
}
