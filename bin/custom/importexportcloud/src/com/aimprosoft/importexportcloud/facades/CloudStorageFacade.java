package com.aimprosoft.importexportcloud.facades;

import static com.aimprosoft.importexportcloud.constants.ImportexportcloudConstants.STORAGE_PATH_SEPARATOR;

import java.util.Collection;

import javax.servlet.http.HttpSession;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.exceptions.IemException;
import com.aimprosoft.importexportcloud.exceptions.ImportException;
import com.aimprosoft.importexportcloud.exceptions.MigrationException;
import com.aimprosoft.importexportcloud.exceptions.TaskException;
import com.aimprosoft.importexportcloud.facades.data.CloudObjectData;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.dropbox.core.DbxException;


public interface CloudStorageFacade
{
	/**
	 * Obtains authentication URL for particular storage type.
	 *
	 * @param storageConfigData storage configuration DTO
	 * @param httpSession       current HTTP session
	 * @return authentication url
	 * @throws CloudStorageException if storage configuration is invalid
	 */
	String getAuthURL(StorageConfigData storageConfigData, HttpSession httpSession) throws CloudStorageException;

	/**
	 * Connects to particular storage.
	 *
	 * @param configData storage configuration DTO
	 * @throws CloudStorageException if storage configuration is invalid
	 */
	void connect(StorageConfigData configData) throws CloudStorageException;

	/**
	 * Downloads specified data from storage.
	 *
	 * @param taskInfoData task information DTO
	 * @return task information DTO with downloaded file path
	 * @throws CloudStorageException if storage configuration is invalid
	 */
	TaskInfoData download(TaskInfoData taskInfoData) throws CloudStorageException;

	/**
	 * Imports specified in <code>taskInfoData</code> zip file.
	 *
	 * @param taskInfoData task information DTO
	 * @return task information DTO
	 * @throws ImportException if an error occurs during importing
	 */
	TaskInfoData importData(TaskInfoData taskInfoData) throws ImportException;

	/**
	 * Exports specified data into zip file and sets its path to <code>taskInfoData</code>.
	 *
	 * @param taskInfoData task information DTO
	 * @return task information DTO
	 * @throws ExportException if an error occurs during exporting process or during result export zip file processing
	 */
	TaskInfoData exportData(TaskInfoData taskInfoData) throws ExportException;

	/**
	 * Upload specified data to storage.
	 *
	 * @param taskInfoData task information DTO
	 * @return task information DTO
	 * @throws CloudStorageException if storage configuration is invalid or an error occurs during uploading
	 */
	TaskInfoData upload(TaskInfoData taskInfoData) throws CloudStorageException;

	/**
	 * Gets a collection of CloudObjectData that represents the content of storage specified directory .
	 * For export task only folders are got. For import task - folders and zip files.
	 *
	 * @param taskInfoData task information DTO
	 * @return collection of cloud object DTO
	 * @throws CloudStorageException if storage configuration is invalid or an error occurs during getting folder content
	 */
	Collection<CloudObjectData> listFiles(TaskInfoData taskInfoData) throws CloudStorageException, DbxException;

	/**
	 * Check whether access token is alive.
	 *
	 * @param selectedConfigData storage configuration DTO
	 * @throws IemException if storage configuration is invalid, an error occurs while connecting to the storage
	 */
	void checkAccessToken(StorageConfigData selectedConfigData) throws IemException;

	/**
	 * Removes either internal exported Media or downloaded from the storage local file.
	 *
	 * @param taskInfoData task information DTO
	 */
	void removeTemporaryFile(TaskInfoData taskInfoData);

	/**
	 * Disconnects the storage.
	 *
	 * @param configData storage configuration DTO
	 * @throws CloudStorageException if storage configuration is invalid or an error occurs while disconnecting
	 */
	void disconnect(StorageConfigData configData) throws CloudStorageException;

	/**
	 * Run migration media via cronjob
	 *
	 * @param taskInfoData task information DTO
	 * @return task information DTO
	 * @throws MigrationException if migration process fails
	 */
	TaskInfoData migrateMediaViaCronJob(TaskInfoData taskInfoData) throws MigrationException;

	/**
	 * Creates CloudObjectData for a root folder.
	 *
	 * @return cloud object DTO
	 */
	static CloudObjectData getCloudDataForRootFolder()
	{
		final CloudObjectData cloudData = new CloudObjectData();

		String name = STORAGE_PATH_SEPARATOR;

		cloudData.setTitle(name);
		cloudData.setPathDisplay(name);
		cloudData.setFolder(Boolean.TRUE);

		return cloudData;
	}

	/**
	 * Removes old data.
	 *
	 * @param taskInfoData task information
	 */
	void removeOldData(TaskInfoData taskInfoData) throws TaskException;

	/**
	 * Synchronization imported data.
	 *
	 * @param taskInfoData task information
	 */
	void synchronizeData(TaskInfoData taskInfoData) throws ImportException, TaskException;

	/**
	 * checks if there are active tasks at this moment. If so, throws IemException
	 *
	 * @throws IemException in case there is more than one active task at this moment
	 */
	void checkActiveTask() throws CloudStorageException;
}
