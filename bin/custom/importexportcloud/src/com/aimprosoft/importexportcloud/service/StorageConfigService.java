package com.aimprosoft.importexportcloud.service;

import com.aimprosoft.importexportcloud.model.StorageConfigModel;
import de.hybris.platform.core.model.user.UserModel;

import java.util.List;


public interface StorageConfigService
{
	/**
	 * Gets a list of storage config models for particular storage type and user that are currently persisted.
	 * If none are found an empty list is returned.
	 *
	 * @param user            user to search for
	 * @param storageTypeCode uniq code of storage type model
	 * @return a list of storage config models
	 */
	List<StorageConfigModel> getAllStorageConfigsByUserAndTypeCode(UserModel user, String storageTypeCode);

	/**
	 * Gets the storage config for the given code.
	 *
	 * @param code the code to search for storage config
	 * @return storage config for code
	 * @throws de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException   in case no storage config for the given code can be found
	 * @throws de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException in case more than one storage config is found for the given code
	 */
	StorageConfigModel getStorageConfigByCode(String code);

	/**
	 * Sets connection status to storage configuration model.
	 *
	 * @param storageConfigCode code for storage configuration model to set status
	 * @param isConnected       connection status
	 */
	void setConnectionStatus(String storageConfigCode, Boolean isConnected);

	/**
	 * Sets access token and connection status to DropBox storage configuration model.
	 *
	 * @param dropBoxConfigCode - code for DropBox storage configuration model to set status
	 * @param accessToken       - access token for DropBox connection
	 * @param isConnected       connection status
	 */
	void setDropBoxConnectionStatus(String dropBoxConfigCode, String accessToken, Boolean isConnected);
}
