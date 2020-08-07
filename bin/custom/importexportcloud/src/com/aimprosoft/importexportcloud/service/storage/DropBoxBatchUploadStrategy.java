package com.aimprosoft.importexportcloud.service.storage;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;

import java.io.File;
import java.io.IOException;


/**
 * Strategy for uploading files to the DropBox by batch chunks
 *
 */
public interface DropBoxBatchUploadStrategy
{
	/**
	 * Uploads files to the DropBox by batch chunks
	 *
	 * @param dbxClient DropBox client
	 * @param dropboxPath Path file to uplaad
	 * @param localFile File to be uploaded
	 * @throws DbxException Throws by dropbox API
	 * @throws IOException Throws when file path is wrong or file is corrupted
	 */

	void uploadBatch(DbxClientV2 dbxClient, String dropboxPath, File localFile) throws DbxException, IOException;
}
