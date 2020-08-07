package com.aimprosoft.importexportcloud.providers.impl;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.providers.DropboxConnectionProvider;
import com.dropbox.core.*;
import com.dropbox.core.v2.DbxClientV2;
import de.hybris.platform.util.Config;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

import javax.servlet.http.HttpSession;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.aimprosoft.importexportcloud.constants.ImportexportcloudConstants.ROOT_FOLDER;


public class DefaultDropboxConnectionProvider implements DropboxConnectionProvider
{
	private static final String DROPBOX_AUTH_CSRF_TOKEN = "dropbox-auth-csrf-token";
	private static final Pattern DROPBOX_PATH_VALIDATION_PATTERN = Pattern.compile("(/(.|[\\r\\n])*|id:.*)|(rev:[0-9a-f]{9,})|(ns:[0-9]+(/.*)?)");

	@Override
	public String getAccessToken(final StorageConfigData storageConfigData, final String clientIdentifier)
			throws DbxException
	{
		final DbxWebAuth dbxWebAuth = getDbxWebAuth(clientIdentifier, storageConfigData.getAppKey(),
				storageConfigData.getEncodedAppSecret());

		final String redirectURL = getRedirectURL(storageConfigData.getStorageTypeData().getCode());

		final DbxAuthFinish dbxAuthFinish = dbxWebAuth.finishFromCode(storageConfigData.getAuthCode(), redirectURL);

		return dbxAuthFinish.getAccessToken();
	}

	@Override
	public DbxClientV2 getDropBoxClient(final String clientIdentifier, final String accessToken)
	{
		final DbxRequestConfig requestConfig = new DbxRequestConfig(clientIdentifier);

		return new DbxClientV2(requestConfig, accessToken);
	}

	@Override
	public String resolveAuthCode(final String[] authCodeArray, final String storageConfigDataCode)
	{
		return ArrayUtils.getLength(authCodeArray) == 2 && storageConfigDataCode.equals(authCodeArray[0])
				? authCodeArray[1]
				: StringUtils.EMPTY;
	}

	@Override
	public boolean isDropBoxPathValid(final String path)
	{
		final Matcher matcher = DROPBOX_PATH_VALIDATION_PATTERN.matcher(path);

		return matcher.matches();
	}

	@Override
	public String getAuthorizeUrl(final StorageConfigData storageConfigData, final HttpSession httpSession,
			final String clientIdentifier)
	{

		final String redirectURL = getRedirectURL(storageConfigData.getStorageTypeData().getCode());

		final DbxWebAuth.Request dbxWebAuthRequest = getDbxWebAuthRequest(httpSession, redirectURL, DROPBOX_AUTH_CSRF_TOKEN);
		final DbxWebAuth dbxWebAuth = getDbxWebAuth(clientIdentifier, storageConfigData.getAppKey(),
				storageConfigData.getEncodedAppSecret());

		return dbxWebAuth.authorize(dbxWebAuthRequest);
	}

	public String getEnsuredPath(String path) throws CloudStorageException
	{
		if (StringUtils.isBlank(path))
		{
			path = ROOT_FOLDER;
		}
		final String normalizedPath = path.startsWith(ROOT_FOLDER) ? path : ROOT_FOLDER + path;
		final Matcher matcher = DROPBOX_PATH_VALIDATION_PATTERN.matcher(normalizedPath);

		if (!matcher.matches())
		{
			throw new CloudStorageException("Invalid path given. Path:" + path);
		}
		return normalizedPath;
	}

	private DbxWebAuth.Request getDbxWebAuthRequest(final HttpSession httpSession, final String redirectURL, final String key)
	{
		final DbxSessionStore dbxSessionStore = new DbxStandardSessionStore(httpSession, key);

		return DbxWebAuth.newRequestBuilder().withForceReapprove(true).withRedirectUri(redirectURL, dbxSessionStore).build();
	}

	private String getRedirectURL(final String storageTypeCode)
	{
		final String keyRedirect = "cloud.storage." + storageTypeCode + ".redirectUrl";

		return Config.getString(keyRedirect, "");
	}

	private DbxWebAuth getDbxWebAuth(final String clientIdentifier, final String appKey, final String appSecret)
	{
		final DbxAppInfo dbxAppInfo = new DbxAppInfo(appKey, appSecret);
		final DbxRequestConfig dbxRequestConfig = new DbxRequestConfig(clientIdentifier);

		return new DbxWebAuth(dbxRequestConfig, dbxAppInfo);
	}
}
