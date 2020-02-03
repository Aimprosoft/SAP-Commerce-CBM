package com.aimprosoft.importexportcloud.service.connection.impl;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.exceptions.IemException;
import com.aimprosoft.importexportcloud.exceptions.InvalidTokenException;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.providers.DropboxConnectionProvider;
import com.dropbox.core.DbxException;
import com.dropbox.core.InvalidAccessTokenException;
import com.dropbox.core.v2.DbxClientV2;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpSession;


public class DefaultDropBoxConnectionService extends AbstractConnectionService
{
	private static final String DROPBOX_AUTH_CODE_ATTRIBUTE = "dropBoxAuthCode";

	private static final Logger LOGGER = Logger.getLogger(DefaultDropBoxConnectionService.class);

	private String clientIdentifier;

	private DropboxConnectionProvider dropboxConnectionProvider;

	@Override
	public void connect(StorageConfigData storageConfigData) throws CloudStorageException
	{
		getStorageConfigValidator().validate(storageConfigData);

		String authCode = getAuthCode(storageConfigData.getCode());
		storageConfigData.setAuthCode(authCode);

		String accessToken = obtainToken(storageConfigData);
		LOGGER.info("Access token has been obtained. Token:" + accessToken);

		getStorageConfigService().setDropBoxConnectionStatus(storageConfigData.getCode(), accessToken, Boolean.TRUE);

		getSessionService().removeAttribute(DROPBOX_AUTH_CODE_ATTRIBUTE);
	}

	@Override
	public String getAuthURL(StorageConfigData storageConfigData, HttpSession httpSession) throws CloudStorageException
	{
		getStorageConfigValidator().validate(storageConfigData);

		String authorizeUrl = dropboxConnectionProvider.getAuthorizeUrl(storageConfigData, httpSession, clientIdentifier);
		LOGGER.info("Authorization URI was generated:" + authorizeUrl);

		return authorizeUrl;
	}

	@Override
	public void revokeToken(StorageConfigData storageConfigData) throws CloudStorageException
	{
		DbxClientV2 client = getClient(storageConfigData);

		try
		{
			client.auth().tokenRevoke();
			clearConfigModelConnectionStatus(storageConfigData.getCode());
			LOGGER.info("Token has been revoked. Access token:" + storageConfigData.getAccessToken());
		}
		catch (InvalidAccessTokenException ex)
		{
			clearConfigModelConnectionStatus(storageConfigData.getCode());
			LOGGER.info("Connection was unlinked from Dropbox application: " + storageConfigData.getCode());
		}
		catch (DbxException e)
		{
			throw new CloudStorageException("Couldn't revoke token", e);
		}
	}

	@Override
	public String obtainPublicURL(TaskInfoData taskInfoData)
	{
		return "";
	}

	private String obtainToken(StorageConfigData storageConfigData) throws CloudStorageException
	{
		String accessToken;

		try
		{
			accessToken = dropboxConnectionProvider.getAccessToken(storageConfigData, clientIdentifier);
		}
		catch (DbxException e)
		{
			throw new CloudStorageException("Access token has not been obtained.", e);
		}

		if (StringUtils.isBlank(accessToken))
		{
			throw new CloudStorageException("Access token is empty.");
		}

		return accessToken;
	}

	@Override
	public void checkAccessToken(StorageConfigData storageConfigData) throws IemException
	{
		DbxClientV2 client = getClient(storageConfigData);

		try
		{
			client.users().getCurrentAccount();
		}
		catch (InvalidAccessTokenException e)
		{
			clearConfigModelConnectionStatus(storageConfigData.getCode());
			throw new InvalidTokenException("Access Token has been expired or incorrect: " + e.getMessage());
		}
		catch (DbxException e)
		{
			throw new CloudStorageException("Dropbox is not available now. Please, try again later: " + e.getMessage(), e);
		}
	}

	private String getAuthCode(String storageConfigDataCode) throws CloudStorageException
	{
		String[] authCodeArray = getSessionService().getAttribute(DROPBOX_AUTH_CODE_ATTRIBUTE);

		String authCode = dropboxConnectionProvider.resolveAuthCode(authCodeArray, storageConfigDataCode);

		if (StringUtils.isBlank(authCode))
		{
			throw new CloudStorageException("Dropbox authorization code is empty.");
		}

		return authCode;
	}

	protected DbxClientV2 getClient(StorageConfigData config) throws CloudStorageException
	{
		getStorageConfigValidator().validate(config);

		if (StringUtils.isBlank(config.getAccessToken()))
		{
			throw new CloudStorageException("Token is expired, check auth please");
		}

		return dropboxConnectionProvider.getDropBoxClient(clientIdentifier, config.getAccessToken());
	}


	private void clearConfigModelConnectionStatus(String dropBoxConfigCode)
	{
		getStorageConfigService().setDropBoxConnectionStatus(dropBoxConfigCode, StringUtils.EMPTY, Boolean.FALSE);
	}

	public String getClientIdentifier()
	{
		return clientIdentifier;
	}

	@Required
	public void setClientIdentifier(String clientIdentifier)
	{
		this.clientIdentifier = clientIdentifier;
	}

	@Required
	public void setDropboxConnectionProvider(
			final DropboxConnectionProvider dropboxConnectionProvider)
	{
		this.dropboxConnectionProvider = dropboxConnectionProvider;
	}
}

