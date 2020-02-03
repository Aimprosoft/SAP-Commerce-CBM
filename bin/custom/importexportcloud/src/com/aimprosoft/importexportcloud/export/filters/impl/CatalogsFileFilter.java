package com.aimprosoft.importexportcloud.export.filters.impl;

import de.hybris.platform.catalog.model.CatalogModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.enums.TaskInfoScope;
import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.export.filters.ExportFileFilter;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import com.aimprosoft.importexportcloud.service.IemCMSSiteService;


public class CatalogsFileFilter implements ExportFileFilter
{
	private IemCMSSiteService iemCmsSiteService;

	@Override
	public void filter(final ExportTaskInfoModel exportTaskInfoModel, final Path filePath) throws ExportException
	{
		final Set<String> catalogNamesToKeep = getCatalogNames(exportTaskInfoModel);

		try (final Stream<String> lines = Files.lines(filePath))
		{
			final List<String> filteredLines = lines
					.filter(line -> (line.contains("#") || catalogNamesToKeep.stream().anyMatch(line::contains)))
					.collect(Collectors.toList());

			Files.write(filePath, filteredLines, StandardOpenOption.CREATE);
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
