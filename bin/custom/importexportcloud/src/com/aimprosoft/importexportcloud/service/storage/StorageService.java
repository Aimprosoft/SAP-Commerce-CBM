package com.aimprosoft.importexportcloud.service.storage;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.data.CloudObjectData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;

import java.util.Collection;


/**
 * Used for working with storage: connect, download, upload, listFiles, obtain publicURL
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
}
