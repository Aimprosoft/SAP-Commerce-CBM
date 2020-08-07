package com.aimprosoft.importexportcloud.service.validators.strategies.impl;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.service.validators.strategies.StorageConfigValidationStrategy;
import org.apache.commons.lang.StringUtils;


public class AWSs3StorageConfigValidationStrategy extends AbstractStorageConfigValidationStrategy implements
		StorageConfigValidationStrategy
{
	@Override
	public void validate(final StorageConfigData storageConfigData) throws CloudStorageException
	{
		super.validate(storageConfigData);
		validateRegionAndBucketName(storageConfigData);
	}

	private void validateRegionAndBucketName(final StorageConfigData storageConfigData) throws CloudStorageException
	{
		if (StringUtils.isBlank(storageConfigData.getRegion()))
		{
			throw new CloudStorageException("Region is empty.");
		}

		if (StringUtils.isBlank(storageConfigData.getBucketName()))
		{
			throw new CloudStorageException("Bucket name is empty.");
		}
	}
}
