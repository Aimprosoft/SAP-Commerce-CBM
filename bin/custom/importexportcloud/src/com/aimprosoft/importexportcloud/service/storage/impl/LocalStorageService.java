package com.aimprosoft.importexportcloud.service.storage.impl;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.data.CloudObjectData;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;


public class LocalStorageService extends AbstractStorageService
{
	private static final Logger LOGGER = Logger.getLogger(LocalStorageService.class);

	@Override
	public TaskInfoData download(final TaskInfoData taskInfoData)
	{
		/* the downloading operation is implemented in Backoffice by creating a temp file */
		final Path path = Paths.get(taskInfoData.getCloudFileDownloadPath());
		taskInfoData.setDownloadedFilePath(path);

		LOGGER.info("File to import path: " + path);

		return taskInfoData;
	}

	@Override
	public TaskInfoData upload(final TaskInfoData taskInfoData)
	{
		/* the uploading operation is implemented in Backoffice by saving file on local machine */
		return taskInfoData;
	}

	@Override
	public long getSize(final StorageConfigData storageConfig, final String location) throws CloudStorageException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream getAsStream(final StorageConfigData storageConfig, final String location) throws CloudStorageException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(final StorageConfigData storageConfig, final String location) throws CloudStorageException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<CloudObjectData> listFiles(final TaskInfoData taskInfoData)
	{
		return Collections.emptyList();
	}

}
