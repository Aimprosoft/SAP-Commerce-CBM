package com.aimprosoft.importexportcloud.providers;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;

import javax.servlet.http.HttpSession;


public interface DropboxConnectionProvider
{
	/**
	 * Gets access token.
	 *
	 * @param storageConfigData storage configuration DTO
	 * @param clientIdentifier  custom name for Dropbox request configuration
	 * @return access token
	 * @throws DbxException if an error occurs while obtaining access token
	 */
	String getAccessToken(StorageConfigData storageConfigData, String clientIdentifier) throws DbxException;

	/**
	 * Creates Dropbox client.
	 *
	 * @param clientIdentifier custom name for Dropbox request configuration
	 * @param accessToken      access token
	 * @return Dropbox client
	 */
	DbxClientV2 getDropBoxClient(String clientIdentifier, String accessToken);

	/**
	 * Finds storage config code in array.
	 *
	 * @param authCodeArray         code array to search in
	 * @param storageConfigDataCode storage config code to search
	 * @return Found storage configuration code or empty string
	 */
	String resolveAuthCode(String[] authCodeArray, String storageConfigDataCode);

	/**
	 * Validates path to match Dropbox path pattern.
	 *
	 * @param path path to validate
	 * @return true if path is valid
	 */
	boolean isDropBoxPathValid(String path);

	/**
	 * Obtains authorization URL from Dropbox.
	 *
	 * @param storageConfigData storage configuration DTO
	 * @param httpSession       current HTTP Session
	 * @param clientIdentifier  custom name for Dropbox request configuration
	 * @return URL for authorization
	 */
	String getAuthorizeUrl(final StorageConfigData storageConfigData, final HttpSession httpSession,
			final String clientIdentifier);


	/**
	 * Obtains valid dropbox path that matches required pattern.
	 *
	 * @param path  Path to match
	 * @return valid path
	 * @throws CloudStorageException if path is not valid
	 */
	String getEnsuredPath(String path) throws CloudStorageException;
}
