package com.aimprosoft.importexportcloud.export.filters.impl;

import com.aimprosoft.importexportcloud.enums.TaskInfoScope;
import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import com.aimprosoft.importexportcloud.service.IemCMSSiteService;
import de.hybris.platform.catalog.model.CatalogModel;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class CatalogsFileExportFilter extends AbstractExportFileFilter
{
	private static final Logger LOG = Logger.getLogger(CatalogsFileExportFilter.class);
	private IemCMSSiteService iemCmsSiteService;

	@Override
	public void filter(final ExportTaskInfoModel exportTaskInfoModel, final Path filePath) throws ExportException
	{
		final Set<String> catalogNamesToKeep = getCatalogNames(exportTaskInfoModel);

		try (Stream<String> streamOfLines = Files.lines(filePath))
		{
			Predicate<String> catalogPredicate = line -> catalogNamesToKeep.stream().anyMatch(line::contains);
			final List<String> filteredLines = filterFileLines(filePath, catalogPredicate);
			logDebug(LOG, "File has been filtered: %s ", filePath);

			copyIntoTargetZip(filteredLines, filePath);

		}
		catch (final IOException e)
		{
			throw new ExportException(String.format("An error occurred while cleaning %s file.", filePath.getFileName().toString()),
					e);
		}
	}

	private Set<String> getCatalogNames(final ExportTaskInfoModel exportTaskInfoModel)
	{
		final TaskInfoScope taskScope = exportTaskInfoModel.getTaskScope();
		Collection<CatalogModel> catalogModels = new HashSet<>();

		if (taskScope.equals(TaskInfoScope.SITESCOPE))
		{
			catalogModels = getIemCmsSiteService().getAllCatalogsForCmsSite(exportTaskInfoModel.getSite());
		}
		else if (taskScope.equals(TaskInfoScope.CATALOGSCOPE))
		{
			catalogModels = Collections.singleton(exportTaskInfoModel.getCatalogVersion().getCatalog());
		}

		return catalogModels.stream().map(CatalogModel::getId).collect(Collectors.toSet());
	}

	public IemCMSSiteService getIemCmsSiteService()
	{
		return iemCmsSiteService;
	}

	@Required
	public void setIemCmsSiteService(final IemCMSSiteService iemCmsSiteService)
	{
		this.iemCmsSiteService = iemCmsSiteService;
	}
}
