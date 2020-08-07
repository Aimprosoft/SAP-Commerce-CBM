package com.aimprosoft.importexportcloud.export.impl;

import com.aimprosoft.importexportcloud.enums.TaskInfoStatus;
import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.export.ExportFileMergeStrategy;
import com.aimprosoft.importexportcloud.export.ExportResultProcessor;
import com.aimprosoft.importexportcloud.export.MediaSeparator;
import com.aimprosoft.importexportcloud.export.filters.ExportFileFilter;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import com.aimprosoft.importexportcloud.model.TaskInfoModel;
import com.aimprosoft.importexportcloud.service.TaskInfoService;

import de.hybris.platform.catalog.model.classification.ClassificationSystemVersionModel;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.impex.model.ImpExMediaModel;
import de.hybris.platform.servicelayer.impex.ExportResult;
import de.hybris.platform.servicelayer.impex.impl.ExportCronJobResult;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.Config;
import de.hybris.platform.util.MediaUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;


public class DefaultExportResultProcessor implements ExportResultProcessor
{
	private static final Logger LOGGER = Logger.getLogger(DefaultExportResultProcessor.class);

	private TaskInfoService<TaskInfoModel> taskInfoService;

	private ModelService modelService;

	private Collection<ExportFileFilter> exportFileFilters;

	private ExportFileMergeStrategy exportFileMergeStrategy;

	private MediaSeparator mediaSeparator;

	@Override
	public void processExportResult(
			final TaskInfoData taskInfoData, final ExportTaskInfoModel exportTaskInfoModel, final ExportResult exportResult)
			throws ExportException
	{
		final CronJobModel cronJob = ((ExportCronJobResult) exportResult).getCronJob();

		if (exportResult.isFinished() && exportResult.isSuccessful())
		{
			final Path fileToUploadPath = modifyDataAndGetFileToUploadPath(exportResult, taskInfoData, exportTaskInfoModel);

			taskInfoData.setFileToUploadPath(fileToUploadPath);

			LOGGER.info("Export zip file was generated successfully. Path: " + fileToUploadPath);

			getTaskInfoService().updateTaskInfo(exportTaskInfoModel, cronJob, TaskInfoStatus.EXPORTED);
		}
		else if (exportResult.isError())
		{
			final String message = "An export file wasn't created.";
			processExportError(exportTaskInfoModel, cronJob, message);
		}
		else
		{
			final String message = "An export file wasn't finished.";
			processExportError(exportTaskInfoModel, cronJob, message);
		}
	}

	private Path modifyDataAndGetFileToUploadPath(final ExportResult exportResult, final TaskInfoData taskInfoData,
			final ExportTaskInfoModel exportTaskInfoModel) throws ExportException
	{
		final ImpExMediaModel exportedData = exportResult.getExportedData();
		final Path exportImpexAndCSVsFilePath = getFilePathFromMediaModel(exportedData);
		final Path serviceFilePath = getTaskInfoService().generateJsonFileWithServiceData(taskInfoData).toPath();

		filterData(exportImpexAndCSVsFilePath, exportTaskInfoModel);

		LOGGER.info("Generated zip file (importscript.impex and csv's) path: " + exportImpexAndCSVsFilePath);

		return executeWithSelectedOptions(taskInfoData, serviceFilePath, exportResult, exportImpexAndCSVsFilePath,
				exportedData, exportTaskInfoModel);
	}

	private Path executeWithSelectedOptions(final TaskInfoData taskInfoData, final Path serviceFilePath,
			final ExportResult exportResult, final Path exportImpexAndCSVsFilePath,
			final ImpExMediaModel exportedData, final ExportTaskInfoModel exportTaskInfoModel) throws ExportException
	{
		String exportedMediaCode;
		final Path resultPath;
		if (taskInfoData.isExportMediaNeeded()
				&& !(exportTaskInfoModel.getCatalogVersion() instanceof ClassificationSystemVersionModel))
		{
			LOGGER.debug("Option \"export process with media\" is enabled");

			resultPath = processResultWithMedia(serviceFilePath, exportResult, exportImpexAndCSVsFilePath);

			final boolean needSeparatingMedia = Config.getBoolean("enable.separating.media", false);
			if (needSeparatingMedia)
			{
				mediaSeparator.separateMedia(resultPath);
			}
			final ImpExMediaModel exportedMedia = exportResult.getExportedMedia();
			exportedMediaCode = exportedMedia.getCode();
			updateMetadata(exportedMedia, taskInfoData);
		}
		else
		{
			resultPath = exportFileMergeStrategy.merge(null, serviceFilePath, exportImpexAndCSVsFilePath);
			exportedMediaCode = exportedData.getCode();
			updateMetadata(exportedData, taskInfoData);
		}
		taskInfoData.setExportedMediaCode(exportedMediaCode);

		return resultPath;
	}

	private void processExportError(final ExportTaskInfoModel exportTaskInfoModel, final CronJobModel cronJob,
			final String exceptionMessage)
			throws ExportException
	{
		getTaskInfoService().updateTaskInfo(exportTaskInfoModel, cronJob, TaskInfoStatus.FAILED);
		throw new ExportException(exceptionMessage, exportTaskInfoModel);
	}

	private Path processResultWithMedia(final Path serviceFilePath, final ExportResult exportResult,
			final Path exportImpexAndCSVsFilePath) throws ExportException
	{
		final Path result;
		final ImpExMediaModel exportedMedia = exportResult.getExportedMedia();

		final Path exportedMediaFilePath = getFilePathFromMediaModel(exportedMedia);

		LOGGER.info("Export media is 'true'. Generated export media zip file path: " + exportedMediaFilePath);

		result = exportFileMergeStrategy.merge(exportedMediaFilePath, serviceFilePath, exportImpexAndCSVsFilePath);

		return result;
	}

	private Path getFilePathFromMediaModel(final ImpExMediaModel exportedData)
	{
		return Paths.get(MediaUtil.getLocalStorageDataDir() + File.separator + exportedData.getLocation());
	}

	private void filterData(final Path exportImpexAndCSVsFilePath, final ExportTaskInfoModel exportTaskInfoModel)
			throws ExportException
	{
		for (final ExportFileFilter filter : getExportFileFilters())
		{
			if (filter.isApplicable(exportTaskInfoModel))
			{
				filter.filter(exportTaskInfoModel, exportImpexAndCSVsFilePath);
			}
		}
	}

	private void updateMetadata(final ImpExMediaModel impExMediaModel, final TaskInfoData taskInfoData)
	{
		impExMediaModel.setRealFileName(taskInfoData.getRealFileName());
		getModelService().save(impExMediaModel);
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	@Required
	public void setTaskInfoService(final TaskInfoService<TaskInfoModel> taskInfoService)
	{
		this.taskInfoService = taskInfoService;
	}

	public TaskInfoService<TaskInfoModel> getTaskInfoService()
	{
		return taskInfoService;
	}

	public Collection<ExportFileFilter> getExportFileFilters()
	{
		return exportFileFilters;
	}

	@Required
	public void setExportFileFilters(final Collection<ExportFileFilter> exportFileFilters)
	{
		this.exportFileFilters = exportFileFilters;
	}

	public ExportFileMergeStrategy getExportFileMergeStrategy()
	{
		return exportFileMergeStrategy;
	}

	@Required
	public void setExportFileMergeStrategy(final ExportFileMergeStrategy exportFileMergeStrategy)
	{
		this.exportFileMergeStrategy = exportFileMergeStrategy;
	}

	@Required
	public void setMediaSeparator(MediaSeparator mediaSeparator)
	{
		this.mediaSeparator = mediaSeparator;
	}
}
