package com.aimprosoft.importexportcloud.service.connection.impl;

import de.hybris.platform.servicelayer.internal.service.AbstractBusinessService;

import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.service.StorageConfigService;
import com.aimprosoft.importexportcloud.service.connection.ConnectionService;
import com.aimprosoft.importexportcloud.service.validators.StorageConfigValidator;


public abstract class AbstractConnectionService extends AbstractBusinessService implements ConnectionService
{
	private StorageConfigService storageConfigService;

	private StorageConfigValidator storageConfigValidator;

	public StorageConfigService getStorageConfigService()
	{
		return storageConfigService;
	}

	@Required
	public void setStorageConfigService(final StorageConfigService storageConfigService)
	{
		this.storageConfigService = storageConfigService;
	}

	public StorageConfigValidator getStorageConfigValidator()
	{
		return storageConfigValidator;
	}

	@Required
	public void setStorageConfigValidator(final StorageConfigValidator storageConfigValidator)
	{
		this.storageConfigValidator = storageConfigValidator;
	}
}
