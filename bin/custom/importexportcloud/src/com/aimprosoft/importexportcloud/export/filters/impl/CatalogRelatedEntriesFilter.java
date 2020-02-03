package com.aimprosoft.importexportcloud.export.filters.impl;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.export.filters.ExportFileFilter;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;


public class CatalogRelatedEntriesFilter implements ExportFileFilter
{
	private Set<String> filesToFilter;

	private Collection<ExportFileFilter> exportFileFilters;

	@Override
	public void filter(final ExportTaskInfoModel exportTaskInfoModel, final Path exportImpexAndCSVsFilePath) throws ExportException
	{
		try (final FileSystem zipFileSystem = FileSystems.newFileSystem(exportImpexAndCSVsFilePath, null))
		{
			for (final String fileName : filesToFilter)
			{
				final Path targetFile = zipFileSystem.getPath(fileName);

				if (Files.exists(targetFile)) //NOSONAR
				{
					processFilters(exportTaskInfoModel, targetFile);
				}
			}
		}
		catch (final IOException e)
		{
			throw new ExportException(
					String.format("An error occurred while opening %s file.", exportImpexAndCSVsFilePath.toString()), e);
		}
	}

	private void processFilters(final ExportTaskInfoModel exportTaskInfoModel, final Path targetFile) throws ExportException
	{
		for (final ExportFileFilter filter : getExportFileFilters())
		{
			if (filter.isApplicable(exportTaskInfoModel))
			{
				filter.filter(exportTaskInfoModel, targetFile);
			}
		}
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
