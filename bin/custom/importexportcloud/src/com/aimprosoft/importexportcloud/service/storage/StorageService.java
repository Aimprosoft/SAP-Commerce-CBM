package com.aimprosoft.importexportcloud.service.storage;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.data.CloudObjectData;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;

import java.io.InputStream;
import java.util.Collection;


/**
 * Used for working with storage: download, upload, listFiles, obtain publicURL
 *
 * @version StorageService v1.0
 */
public interface StorageService
{
	/**
	 * Downloads content from the cloud.
	 *
	 * @param taskInfoData task information
	 * @return modified storage configuration data
	 * @throws CloudStorageException if credentials are invalid or an error occurred during the download
	 */
	TaskInfoData download(TaskInfoData taskInfoData) throws CloudStorageException;

	/**
	 * Uploads content to the cloud.
	 *
	 * @param taskInfoData task information
	 * @return modified storage configuration data
	 * @throws CloudStorageException if credentials are invalid or an error occurred during the upload
	 */
	TaskInfoData upload(TaskInfoData taskInfoData) throws CloudStorageException;

	/**
	 * Gets content of the directory in the storage.
	 *
	 * @param taskInfoData task information
	 * @return collection of CloudObjectData
	 * @throws CloudStorageException if credentials are invalid or an error occurred during getting the list
	 */
	Collection<CloudObjectData> listFiles(TaskInfoData taskInfoData) throws CloudStorageException;

	/**
	 * Get size for file.
	 *
	 * @param storageConfig storage config data
	 * @param location      file location
	 * @return long of file size
	 * @throws CloudStorageException if credentials are invalid or an error occurred during getting the list
	 */
	long getSize(StorageConfigData storageConfig, String location) throws CloudStorageException;

	/**
	 * Get Input Stream for file in the storage.
	 *
	 * @param storageConfig storage config data
	 * @param location      file location
	 * @return an input stream file in the storage
	 * @throws CloudStorageException if credentials are invalid or an error occurred during getting the list
	 */
	InputStream getAsStream(StorageConfigData storageConfig, String location) throws CloudStorageException;

	/**
	 * Remove file in the storage.
	 *
	 * @param storageConfig storage config data
	 * @param location      file location
	 * @throws CloudStorageException if credentials are invalid or an error occurred during getting the list
	 */
	void delete(StorageConfigData storageConfig, String location) throws CloudStorageException;

}
