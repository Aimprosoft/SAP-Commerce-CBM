package com.aimprosoft.importexportcloud.export.filters.impl;

import de.hybris.platform.catalog.model.classification.ClassificationSystemModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.enums.TaskInfoScope;
import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.export.filters.ExportFileFilter;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;


public class ClassificationCatalogRelationFilter implements ExportFileFilter
{
	private Set<String> filesToFilter;

	@Override
	public boolean isApplicable(final ExportTaskInfoModel exportTaskInfoModel)
	{
		final TaskInfoScope taskScope = exportTaskInfoModel.getTaskScope();

		return taskScope.equals(TaskInfoScope.CATALOGSCOPE) && exportTaskInfoModel.getCatalogVersion()
				.getCatalog() instanceof ClassificationSystemModel;
	}

	@Override
	public void filter(final ExportTaskInfoModel exportTaskInfoModel, final Path exportImpexAndCSVsFilePath) throws ExportException
	{
		final Path fileName = exportImpexAndCSVsFilePath.getFileName();

		final String classificationsCatalogId = exportTaskInfoModel.getCatalogVersion().getCatalog().getId();

		if (filesToFilter.contains(fileName.toString()))
		{
			filterClassificationCatalogRelation(exportImpexAndCSVsFilePath, classificationsCatalogId);
		}
	}

	private void filterClassificationCatalogRelation(final Path file, final String classificationsCatalogId) throws ExportException
	{
		try (final Stream<String> lines = Files.lines(file))
		{
			/*get only classification to classification relation*/
			final List<String> filteredLines = lines
					.filter(line -> line.contains("#") || StringUtils.countMatches(line, classificationsCatalogId) == 2)
					.collect(Collectors.toList());

			Files.write(file, filteredLines, StandardOpenOption.CREATE);
		}
		catch (final IOException e)
		{
			throw new ExportException(String.format("An error occurred while cleaning %s file.", file.getFileName().toString()), e);
		}
	}

	public Set<String> getFilesToFilter()
	{
		return filesToFilter;
	}

	@Required
	public void setFilesToFilter(final Set<String> filesToFilter)
	{
		this.filesToFilter = filesToFilter;
	}

}

