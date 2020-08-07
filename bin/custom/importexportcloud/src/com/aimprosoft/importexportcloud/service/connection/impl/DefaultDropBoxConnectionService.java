package com.aimprosoft.importexportcloud.service.connection.impl;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.exceptions.IemException;
import com.aimprosoft.importexportcloud.exceptions.InvalidTokenException;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.providers.DropboxConnectionProvider;
import com.dropbox.core.DbxException;
import com.dropbox.core.InvalidAccessTokenException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.sharing.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpSession;
import java.util.Date;


public class DefaultDropBoxConnectionService extends AbstractConnectionService
{
	private static final Logger LOGGER = Logger.getLogger(DefaultDropBoxConnectionService.class);

	private static final String DROPBOX_AUTH_CODE_ATTRIBUTE = "dropBoxAuthCode";

	private static final String PREVIEW_URL = "?dl=0";

	private static final String DOWNLOAD_URL = "?raw=1";

	private String clientIdentifier;

	private DropboxConnectionProvider dropboxConnectionProvider;

	@Override
	public void connect(final StorageConfigData storageConfigData) throws CloudStorageException
	{
		getStorageConfigValidator().validate(storageConfigData);

		final String authCode = getAuthCode(storageConfigData.getCode());
		storageConfigData.setAuthCode(authCode);

		final String accessToken = obtainToken(storageConfigData);
		LOGGER.info("Access token has been obtained. Token:" + accessToken);

		getStorageConfigService().setDropBoxConnectionStatus(storageConfigData.getCode(), accessToken, Boolean.TRUE);

		getSessionService().removeAttribute(DROPBOX_AUTH_CODE_ATTRIBUTE);
	}

	@Override
	public String getAuthURL(final StorageConfigData storageConfigData, final HttpSession httpSession) throws CloudStorageException
	{
		getStorageConfigValidator().validate(storageConfigData);

		final String authorizeUrl = dropboxConnectionProvider.getAuthorizeUrl(storageConfigData, httpSession, clientIdentifier);
		LOGGER.info("Authorization URI was generated:" + authorizeUrl);

		return authorizeUrl;
	}

	@Override
	public void revokeToken(final StorageConfigData storageConfigData) throws CloudStorageException
	{
		final DbxClientV2 client = getClient(storageConfigData);

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
	public String obtainPublicURL(final StorageConfigData storageConfig, final String location) throws CloudStorageException
	{
		final Date expires = getDateOfExpiry(storageConfig);

		final DbxClientV2 client = getClient(storageConfig);
		final String path = dropboxConnectionProvider.getEnsuredPath(location);
		final DbxUserSharingRequests shareRequest = client.sharing();
		try
		{
			final ListSharedLinksResult result = shareRequest.listSharedLinksBuilder().withPath(path).start();

			return CollectionUtils.emptyIfNull(result.getLinks()).stream()
					.map(SharedLinkMetadata::getUrl)
					.filter(StringUtils::isNotEmpty)
					.map(this::getNormalizedLink)
					.findFirst()
					.orElseGet(() -> createNewPublicURL(shareRequest, path, expires));
		}
		catch (DbxException e)
		{
			throw new CloudStorageException("Couldn't obtain public URL", e);
		}
	}

	private String createNewPublicURL(final DbxUserSharingRequests sharingRequests, final String location, final Date expires)
	{
		final SharedLinkSettings settings = new SharedLinkSettings(RequestedVisibility.PUBLIC, null, expires);
		try
		{
			/*
			 * DropBox basic user can't set expires date for URL. So make sure that yoour dropbox user have such rights
			 *
			 * not_authorized Void User is not allowed to modify the settings of this link. Note that basic users can only
			 * set RequestedVisibility.public as the SharedLinkSettings.requested_visibility and cannot set SharedLinkSettings.expires.
			 * See https://www.dropbox.com/developers/documentation/http/documentation#sharing-create_shared_link_with_settings
			 * */

			final SharedLinkMetadata metadata = sharingRequests.createSharedLinkWithSettings(location, settings);

			return getNormalizedLink(metadata.getUrl());
		}
		catch (DbxException e)
		{
			LOGGER.warn("Could not create public DropBox URL for " + location, e);
		}
		return null;
	}

	private String getNormalizedLink(final String url)
	{
		return url.replace(PREVIEW_URL, DOWNLOAD_URL);
	}

	private String obtainToken(final StorageConfigData storageConfigData) throws CloudStorageException
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
	public void checkAccessToken(final StorageConfigData storageConfigData) throws IemException
	{
		final DbxClientV2 client = getClient(storageConfigData);

		try
		{
			client.users().getCurrentAccount();
		}
		catch (InvalidAccessTokenException e)
		{
			clearConfigModelConnectionStatus(storageConfigData.getCode());
			throw new InvalidTokenException("Access Token has been expired or incorrect " + e.getMessage(), e);
		}
		catch (DbxException e)
		{
			throw new CloudStorageException("Dropbox is not available now. Please, try again later " + e.getMessage(), e);
		}
	}

	private String getAuthCode(final String storageConfigDataCode) throws CloudStorageException
	{
		final String[] authCodeArray = getSessionService().getAttribute(DROPBOX_AUTH_CODE_ATTRIBUTE);

		final String authCode = dropboxConnectionProvider.resolveAuthCode(authCodeArray, storageConfigDataCode);

		if (StringUtils.isBlank(authCode))
		{
			throw new CloudStorageException("Dropbox authorization code is empty.");
		}

		return authCode;
	}

	protected DbxClientV2 getClient(final StorageConfigData config) throws CloudStorageException
	{
		getStorageConfigValidator().validate(config);

		if (StringUtils.isBlank(config.getAccessToken()))
		{
			throw new CloudStorageException("Token is expired, check auth please");
		}

		return dropboxConnectionProvider.getDropBoxClient(clientIdentifier, config.getAccessToken());
	}


	private void clearConfigModelConnectionStatus(final String dropBoxConfigCode)
	{
		getStorageConfigService().setDropBoxConnectionStatus(dropBoxConfigCode, StringUtils.EMPTY, Boolean.FALSE);
	}

	public String getClientIdentifier()
	{
		return clientIdentifier;
	}

	@Required
	public void setClientIdentifier(final String clientIdentifier)
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

