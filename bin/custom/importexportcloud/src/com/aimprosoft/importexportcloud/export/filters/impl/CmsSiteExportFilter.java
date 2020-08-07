package com.aimprosoft.importexportcloud.export.filters.impl;

import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;


public class CmsSiteExportFilter extends AbstractSiteRelatedExportFilter
{
	private static final Logger LOG = Logger.getLogger(CmsSiteExportFilter.class);

	private Set<String> filesToFilter;

	@Override
	public void filter(final ExportTaskInfoModel exportTaskInfoModel, final Path exportImpexAndCSVsFilePath) throws ExportException
	{
		final String siteToExport = exportTaskInfoModel.getSite().getName();
		Path targetFilePath = null;
		try (final FileSystem zipFileSystem = FileSystems.newFileSystem(exportImpexAndCSVsFilePath, null))
		{
			for (final String fileName : filesToFilter)
			{
				targetFilePath = zipFileSystem.getPath(fileName);

				Predicate<String> catalogPredicate = line ->  line.contains(siteToExport);
				final List<String> filteredLines = filterFileLines(targetFilePath, catalogPredicate);
				logDebug(LOG, "File lines for site %s are filtering. Target File: %s, path: %s ",
						siteToExport, targetFilePath.getFileName(), targetFilePath.toAbsolutePath());

				copyIntoTargetZip(filteredLines, targetFilePath);
			}
		}
		catch (IOException e)
		{
			throw new ExportException(String.format("An error occurred while cleaning %s file.",
					targetFilePath != null ? targetFilePath.getFileName().toString() : null), e);
		}
	}

	@Required
	public void setFilesToFilter(final Set<String> filesToFilter)
	{
		this.filesToFilter = filesToFilter;
	}
}
