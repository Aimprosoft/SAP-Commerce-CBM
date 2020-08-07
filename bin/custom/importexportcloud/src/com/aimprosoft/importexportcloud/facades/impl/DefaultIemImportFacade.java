package com.aimprosoft.importexportcloud.facades.impl;

import de.hybris.platform.servicelayer.impex.ImportConfig;
import de.hybris.platform.servicelayer.impex.ImportResult;
import de.hybris.platform.servicelayer.impex.ImportService;
import de.hybris.platform.servicelayer.impex.impl.FileBasedImpExResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.hybris.platform.util.Config;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.enums.TaskInfoStatus;
import com.aimprosoft.importexportcloud.exceptions.ImportException;
import com.aimprosoft.importexportcloud.facades.IemImportFacade;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.TaskInfoModel;
import com.aimprosoft.importexportcloud.service.TaskInfoService;


public class DefaultIemImportFacade implements IemImportFacade
{
	private static final Logger LOGGER = Logger.getLogger(DefaultIemImportFacade.class);
	private static final String IMPEX_ENCODING = "UTF-8";
	private static final String FILE_SEPARATOR = File.separator;

	private ImportService importService;

	private TaskInfoService<TaskInfoModel> taskInfoService;

	//implemented only for ZIP files with external resources
	@Override
	public TaskInfoData importData(final TaskInfoData taskInfoData) throws ImportException
	{
		final TaskInfoModel taskInfo = taskInfoService.getTaskByCode(taskInfoData.getTaskInfoCode());
		taskInfoService.setTaskInfoStatus(taskInfo, TaskInfoStatus.IMPORTING);

		logStartingImport(taskInfoData);

		final ImportConfig importConfig = getImportConfig(taskInfoData, taskInfo);

		boolean isImportByBatchNeeded = Config.getBoolean("need.import.by.batch", false);

		if (isImportByBatchNeeded)
		{
			importMediaByBatch(taskInfo, taskInfoData);
		}

		final ImportResult importResult = importService.importData(importConfig);

		taskInfoService.setTaskInfoCronJob(taskInfo, importResult.getCronJob());

		if (importResult.isSuccessful())
		{
			taskInfoService.setTaskInfoStatus(taskInfo, TaskInfoStatus.IMPORTED);
		}
		else
		{
			taskInfoService.setTaskInfoStatus(taskInfo, TaskInfoStatus.FAILED);
			throw new ImportException(StringUtils.EMPTY, taskInfo);
		}

		taskInfoService.setTaskInfoFinishedDate(taskInfo);

		return taskInfoData;
	}

	private void importMediaByBatch(final TaskInfoModel taskInfo, final TaskInfoData taskInfoData) throws ImportException
	{
		final Path zipFolderPath = taskInfoData.getDownloadedFilePath();
		final Path parentDirPath = zipFolderPath.getParent();
		try (final ZipFile archive = new ZipFile(zipFolderPath.toString()))
		{
			final Enumeration<? extends ZipEntry> zipEntries = archive.entries();

			while (zipEntries.hasMoreElements())
			{
				final ZipEntry zipEntry = zipEntries.nextElement();
				if (zipEntry.getName().contains(".zip"))
				{
					final Path fileToCreate = parentDirPath.resolve(zipEntry.getName());
					try (final InputStream inputStream = archive.getInputStream(zipEntry))
					{
						Files.copy(inputStream, fileToCreate, StandardCopyOption.REPLACE_EXISTING);
					}
					final Path tempMediaFilePath = Paths.get(parentDirPath + FILE_SEPARATOR + zipEntry.getName());
					taskInfoData.setDownloadedFilePath(tempMediaFilePath);
					final ImportConfig importConfig = getImportConfig(taskInfoData, taskInfo);

					importService.importData(importConfig);
					removeTempFiles(tempMediaFilePath);
				}
			}
			taskInfoData.setDownloadedFilePath(zipFolderPath);
		}
		catch (IOException ex)
		{
			throw new ImportException("An error occurred while importing data ", taskInfo);
		}
	}

	private void removeTempFiles(Path tempFilePath)
	{
		try
		{
			Files.delete(tempFilePath);
		}
		catch (IOException e)
		{
			LOGGER.warn("Temp file is not removed.");
		}
	}

	private ImportConfig getImportConfig(final TaskInfoData taskInfoData, final TaskInfoModel taskInfoModel) throws ImportException
	{
		final ImportConfig config;
		final Path downloadedFilePath = taskInfoData.getDownloadedFilePath();
		if (downloadedFilePath != null)
		{
			config = new ImportConfig();

			config.setRemoveOnSuccess(false);
			config.setLegacyMode(true);
			config.setEnableCodeExecution(true);
			config.setDistributedImpexEnabled(false);

			final FileBasedImpExResource scriptImpExResource = new FileBasedImpExResource(downloadedFilePath.toFile(),
					IMPEX_ENCODING);
			config.setScript(scriptImpExResource);

			if (taskInfoData.isExportMediaNeeded())
			{
				config.setMediaArchive(scriptImpExResource);
			}
		}
		else
		{
			throw new ImportException("The downloaded path is empty.", taskInfoModel);
		}

		return config;
	}

	private void logStartingImport(final TaskInfoData taskInfo)
	{
		final String configName = taskInfo.getConfig().getName();
		final String cloudFileDownloadPathToDisplay = taskInfo.getCloudFileDownloadPathToDisplay();
		final String cloudFileDownloadPath = taskInfo.getCloudFileDownloadPath();
		final String taskInfoCode = taskInfo.getTaskInfoCode();

		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug(String.format("Starting import file from: %s, storage path: %s, from account: %s, task code: %s",
					cloudFileDownloadPathToDisplay, cloudFileDownloadPath, configName, taskInfoCode));
		}
	}

	public ImportService getImportService()
	{
		return importService;
	}

	@Required
	public void setImportService(final ImportService importService)
	{
		this.importService = importService;
	}


	public TaskInfoService<TaskInfoModel> getTaskInfoService()
	{
		return taskInfoService;
	}

	@Required
	public void setTaskInfoService(final TaskInfoService<TaskInfoModel> taskInfoService)
	{
		this.taskInfoService = taskInfoService;
	}
}
