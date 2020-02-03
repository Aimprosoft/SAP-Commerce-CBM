package com.aimprosoft.importexportcloud.service.connection.impl;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;

import javax.servlet.http.HttpSession;


public class DefaultLocalConnectionService extends AbstractConnectionService
{
	@Override
	public void connect(final StorageConfigData storageConfigData)
	{
		/* no connection needed */
	}

	@Override
	public String getAuthURL(final StorageConfigData storageConfigData, final HttpSession httpSession)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String obtainPublicURL(final TaskInfoData taskInfoData)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void revokeToken(final StorageConfigData storageConfigData)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void checkAccessToken(final StorageConfigData storageConfigData)
	{
		throw new UnsupportedOperationException();
	}
}
