package com.aimprosoft.importexportcloud.facades.impl;

import static com.aimprosoft.importexportcloud.constants.ImportexportcloudConstants.STORAGE_PATH_SEPARATOR;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.servicelayer.impex.ExportConfig;
import de.hybris.platform.servicelayer.impex.ExportResult;
import de.hybris.platform.servicelayer.impex.ExportService;
import de.hybris.platform.servicelayer.impex.ImpExResource;
import de.hybris.platform.servicelayer.impex.impl.StreamBasedImpExResource;
import de.hybris.platform.util.CSVConstants;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.enums.TaskInfoScope;
import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.export.ExportResultProcessor;
import com.aimprosoft.importexportcloud.export.ExportScriptGeneratorService;
import com.aimprosoft.importexportcloud.facades.ExportFacade;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import com.aimprosoft.importexportcloud.model.TaskInfoModel;
import com.aimprosoft.importexportcloud.service.IemCatalogVersionService;
import com.aimprosoft.importexportcloud.service.TaskInfoService;


public class DefaultExportFacade implements ExportFacade
{
	private static final Logger LOGGER = Logger.getLogger(DefaultExportFacade.class);

	private ExportScriptGeneratorService exportScriptGeneratorService;

	private ExportService exportService;

	private TaskInfoService<TaskInfoModel> taskInfoService;

	private IemCatalogVersionService iemCatalogVersionService;

	private ExportResultProcessor exportResultProcessor;

	@Override
	public TaskInfoData exportData(final TaskInfoData taskInfoData) throws ExportException
	{
		final ExportTaskInfoModel exportTaskInfoModel = taskInfoService.createExportTaskInfoModel(taskInfoData);
		taskInfoData.setTaskInfoCode(exportTaskInfoModel.getCode());
		final String fileName = StringUtils.substringAfterLast(exportTaskInfoModel.getExternalPath(), STORAGE_PATH_SEPARATOR);
		taskInfoData.setRealFileName(fileName);

		final ExportResult exportResult = exportDataInternal(exportTaskInfoModel);

		exportResultProcessor.processExportResult(taskInfoData, exportTaskInfoModel, exportResult);

		return taskInfoData;
	}

	private ExportResult exportDataInternal(final ExportTaskInfoModel exportTaskInfoModel)
	{
		logStartingExport(exportTaskInfoModel);

		final ExportConfig exportConfig = getExportConfig(exportTaskInfoModel);

		final Collection<CatalogVersionModel> catalogVersionsToExportFrom = getCatalogVersionsToExportFrom(exportTaskInfoModel);

		return iemCatalogVersionService
				.executeWithCatalogVersions(() -> exportService.exportData(exportConfig), catalogVersionsToExportFrom);
	}

	private void logStartingExport(final ExportTaskInfoModel exportTaskInfoModel)
	{
		if (LOGGER.isDebugEnabled())
		{
			final TaskInfoScope taskScope = exportTaskInfoModel.getTaskScope();
			final String storageType = exportTaskInfoModel.getStorageConfig().getType().getName();
			String exportItem = StringUtils.EMPTY;

			if (TaskInfoScope.CATALOGSCOPE.equals(taskScope))
			{
				final CatalogVersionModel catalogVersion = exportTaskInfoModel.getCatalogVersion();
				exportItem = catalogVersion.getCatalog().getName() + ":" + catalogVersion.getVersion();
			}
			else if (TaskInfoScope.SITESCOPE.equals(taskScope))
			{
				exportItem = exportTaskInfoModel.getSite().getName();
			}

			LOGGER.debug(String.format("Export scope: '%s'. Item to export: '%s'. Storage type: '%s'. Starting export...", taskScope,
					exportItem, storageType));
		}
	}

	private Collection<CatalogVersionModel> getCatalogVersionsToExportFrom(final ExportTaskInfoModel exportTaskInfoModel)
	{
		final TaskInfoScope taskScope = exportTaskInfoModel.getTaskScope();

		final Collection<CatalogVersionModel> catalogVersionModels;

		switch (taskScope)
		{
			case CATALOGSCOPE:
				catalogVersionModels = Collections.singleton(exportTaskInfoModel.getCatalogVersion());
				break;

			case SITESCOPE:
				catalogVersionModels = iemCatalogVersionService.getExportableCatalogVersionsForCmsSite(exportTaskInfoModel.getSite());
				break;

			case ITEMSCOPE:
				catalogVersionModels = Collections.emptySet();
				break;

			default:
				catalogVersionModels = null;
				break;
		}

		return catalogVersionModels;
	}

	private ExportConfig getExportConfig(final ExportTaskInfoModel exportTaskInfoModel)
	{
		final ExportConfig exportConfig = new ExportConfig();
		exportConfig.setScript(getExportScriptImpExResource(exportTaskInfoModel));
		exportConfig.setValidationMode(ExportConfig.ValidationMode.STRICT);

		return exportConfig;
	}

	private ImpExResource getExportScriptImpExResource(final ExportTaskInfoModel exportTaskInfoModel)
	{
		final String script = exportScriptGeneratorService.generateExportScript(exportTaskInfoModel);

		return new StreamBasedImpExResource(new ByteArrayInputStream(script.getBytes()),
				CSVConstants.HYBRIS_ENCODING, CSVConstants.DEFAULT_FIELD_SEPARATOR);
	}

	public ExportScriptGeneratorService getExportScriptGeneratorService()
	{
		return exportScriptGeneratorService;
	}

	@Required
	public void setExportScriptGeneratorService(final ExportScriptGeneratorService exportScriptGeneratorService)
	{
		this.exportScriptGeneratorService = exportScriptGeneratorService;
	}

	public ExportService getExportService()
	{
		return exportService;
	}

	@Required
	public void setExportService(final ExportService exportService)
	{
		this.exportService = exportService;
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

	public ExportResultProcessor getExportResultProcessor()
	{
		return exportResultProcessor;
	}

	@Required
	public void setExportResultProcessor(final ExportResultProcessor exportResultProcessor)
	{
		this.exportResultProcessor = exportResultProcessor;
	}

	public IemCatalogVersionService getIemCatalogVersionService()
	{
		return iemCatalogVersionService;
	}

	@Required
	public void setIemCatalogVersionService(final IemCatalogVersionService iemCatalogVersionService)
	{
		this.iemCatalogVersionService = iemCatalogVersionService;
	}
}
