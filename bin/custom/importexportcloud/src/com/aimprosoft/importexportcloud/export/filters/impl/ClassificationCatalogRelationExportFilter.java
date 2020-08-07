package com.aimprosoft.importexportcloud.export.filters.impl;

import com.aimprosoft.importexportcloud.enums.TaskInfoScope;
import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import de.hybris.platform.catalog.model.classification.ClassificationSystemModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;


public class ClassificationCatalogRelationExportFilter extends AbstractExportFileFilter
{
	private static final Logger LOG = Logger.getLogger(ClassificationCatalogRelationExportFilter.class);
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
		logDebug(LOG, String.format("Filtering classification catalog with id: %s", classificationsCatalogId));

		if (filesToFilter.contains(fileName.toString()))
		{
			filterClassificationCatalogRelation(exportImpexAndCSVsFilePath, classificationsCatalogId);
		}
	}

	private void filterClassificationCatalogRelation(final Path filePath, final String classificationsCatalogId) throws ExportException
	{
		try
		{
			Predicate<String> catalogPredicate = line ->  StringUtils.countMatches(line, classificationsCatalogId) == 2;
			final List<String> filteredLines = filterFileLines(filePath, catalogPredicate);
			logDebug(LOG, "File lines filtered for catalog %s and file %s ", classificationsCatalogId, filePath.toAbsolutePath());

			copyIntoTargetZip(filteredLines, filePath);
		}
		catch (final IOException e)
		{
			throw new ExportException(String.format("An error occurred while cleaning %s file.", filePath.getFileName().toString()), e);
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

