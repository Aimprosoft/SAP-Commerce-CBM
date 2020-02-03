package com.aimprosoft.importexportcloud.service.connection.impl;

import javax.servlet.http.HttpSession;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.providers.S3ConnectionProvider;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;


public class DefaultS3ConnectionService extends AbstractConnectionService
{
	private static final Logger LOGGER = Logger.getLogger(DefaultS3ConnectionService.class);

	private S3ConnectionProvider s3ConnectionProvider;

	@Override
	public void connect(final StorageConfigData storageConfigData) throws CloudStorageException
	{
		final S3Client s3Client = getS3Client(storageConfigData);

		checkConnection(s3Client);

		getStorageConfigService().setConnectionStatus(storageConfigData.getCode(), Boolean.TRUE);
	}

	/*
	 * 'revokeToken' method is used for disconnecting AWS S3 account.
	 *	 This part of functionality implemented in 'revokeToken' for unification 'Disconnect' button functionality.
	 */
	@Override
	public void revokeToken(final StorageConfigData storageConfigData)
	{
		storageConfigData.setIsConnected(Boolean.FALSE);
		getStorageConfigService().setConnectionStatus(storageConfigData.getCode(), Boolean.FALSE);
	}

	@Override
	public void checkAccessToken(final StorageConfigData storageConfigData)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getAuthURL(final StorageConfigData storageConfigData, final HttpSession httpSession)
	{
		throw new UnsupportedOperationException("");
	}

	@Override
	public String obtainPublicURL(final TaskInfoData taskInfoData)
	{
		throw new UnsupportedOperationException("");
	}

	protected S3Client getS3Client(final StorageConfigData storageConfigData) throws CloudStorageException
	{
		getStorageConfigValidator().validate(storageConfigData);

		return s3ConnectionProvider.getS3Client(storageConfigData);
	}

	private void checkConnection(final S3Client s3Client) throws CloudStorageException
	{
		final ListBucketsResponse listBucketsResponse;

		try
		{
			listBucketsResponse = s3Client.listBuckets();
		}
		catch (final SdkException e)
		{
			throw new CloudStorageException("Connection failed.", e);
		}

		if (CollectionUtils.isEmpty(listBucketsResponse.buckets()))
		{
			throw new CloudStorageException("Buckets have not found.");
		}

		LOGGER.info("Connection to Amazon has been checked.");
	}

	@Required
	public void setS3ConnectionProvider(final S3ConnectionProvider s3ConnectionProvider)
	{
		this.s3ConnectionProvider = s3ConnectionProvider;
	}
}
