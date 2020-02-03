package com.aimprosoft.importexportcloud.service.connection;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.exceptions.IemException;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;

import javax.servlet.http.HttpSession;


public interface ConnectionService
{
	/**
	 * Connects to the cloud.
	 *
	 * @param storageConfigData storage configuration data
	 * @throws CloudStorageException if credentials are invalid or an error occurred during the connection
	 */
	void connect(StorageConfigData storageConfigData) throws CloudStorageException;

	/**
	 * Check connection to the cloud.
	 *
	 * @param storageConfigData storage configuration data
	 * @throws CloudStorageException if credentials are invalid or an error occurred during the connection
	 */
	void checkAccessToken(StorageConfigData storageConfigData) throws IemException;

	/**
	 * Revokes the access token.
	 *
	 * @throws CloudStorageException if credentials are invalid or an error occurred during revoking
	 */
	void revokeToken(StorageConfigData storageConfigData) throws CloudStorageException;

	/**
	 * Obtains URL for authorization.
	 *
	 * @param storageConfigData storage configuration data
	 * @param httpSession       current session
	 * @return URL for authorization
	 * @throws CloudStorageException if credentials are invalid
	 */
	String getAuthURL(StorageConfigData storageConfigData, HttpSession httpSession) throws CloudStorageException;

	/**
	 * Obtains the public URL.
	 *
	 * @param taskInfoData task information
	 * @return public URL
	 */
	String obtainPublicURL(TaskInfoData taskInfoData);
}
