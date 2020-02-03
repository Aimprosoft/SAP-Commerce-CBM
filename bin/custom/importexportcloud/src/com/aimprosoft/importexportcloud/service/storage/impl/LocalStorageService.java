package com.aimprosoft.importexportcloud.service.storage.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;

import com.aimprosoft.importexportcloud.facades.data.CloudObjectData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;


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
	public Collection<CloudObjectData> listFiles(final TaskInfoData taskInfoData)
	{
		return Collections.emptyList();
	}

}
