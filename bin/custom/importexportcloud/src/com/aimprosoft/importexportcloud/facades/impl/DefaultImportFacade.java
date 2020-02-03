package com.aimprosoft.importexportcloud.facades.impl;

import de.hybris.platform.servicelayer.impex.ImportConfig;
import de.hybris.platform.servicelayer.impex.ImportResult;
import de.hybris.platform.servicelayer.impex.ImportService;
import de.hybris.platform.servicelayer.impex.impl.FileBasedImpExResource;

import java.nio.file.Path;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.enums.TaskInfoStatus;
import com.aimprosoft.importexportcloud.exceptions.ImportException;
import com.aimprosoft.importexportcloud.facades.ImportFacade;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.TaskInfoModel;
import com.aimprosoft.importexportcloud.service.TaskInfoService;


public class DefaultImportFacade implements ImportFacade
{
	private static final Logger LOGGER = Logger.getLogger(DefaultImportFacade.class);
	private static final String COULDN_T_IMPORT_IMPEX = "Couldn't import impex";
	private static final String IMPEX_ENCODING = "UTF-8";

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
		final ImportResult importResult = importService.importData(importConfig);

		taskInfoService.setTaskInfoCronJob(taskInfo, importResult.getCronJob());

		if (importResult.isSuccessful())
		{
			taskInfoService.setTaskInfoStatus(taskInfo, TaskInfoStatus.IMPORTED);
		}
		else
		{
			taskInfoService.setTaskInfoStatus(taskInfo, TaskInfoStatus.FAILED);
			throw new ImportException(COULDN_T_IMPORT_IMPEX, taskInfo);
		}

		taskInfoService.setTaskInfoFinishedDate(taskInfo);

		return taskInfoData;
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

			final FileBasedImpExResource scriptImpExResource = new FileBasedImpExResource(downloadedFilePath.toFile(), IMPEX_ENCODING);
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
