package com.aimprosoft.importexportcloud.export.filters.impl;

import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.export.filters.ExportFileFilter;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import de.hybris.platform.acceleratorservices.model.SiteMapConfigModel;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;


public class CatalogUnawareMediaExportFilter extends AbstractSiteRelatedExportFilter implements ExportFileFilter
{
	private static final Logger LOG = Logger.getLogger(CatalogUnawareMediaExportFilter.class);
	private Set<String> filesToFilter;

	@Override
	public void filter(final ExportTaskInfoModel exportTaskInfoModel, final Path exportImpexAndCSVsFilePath) throws ExportException
	{
		try (final FileSystem zipFileSystem = FileSystems.newFileSystem(exportImpexAndCSVsFilePath, null))
		{
			for (final String fileName : filesToFilter)
			{
				final SiteMapConfigModel siteMapConfig = exportTaskInfoModel.getSite().getSiteMapConfig();
				final String mediaContentCode = siteMapConfig.getSiteMapTemplate().getContent().getCode();

				final Path targetFilePath = zipFileSystem.getPath(fileName);

				Predicate<String> catalogPredicate = line -> line.contains(mediaContentCode);
				final List<String> filteredLines = filterFileLines(targetFilePath, catalogPredicate);
				logDebug(LOG, "File has been filtered: %s, media content code: %s ", targetFilePath, mediaContentCode);

				copyIntoTargetZip(filteredLines, targetFilePath);
			}
		}
		catch (IOException e)
		{
			throw new ExportException("An error occurred while cleaning archive", e);
		}
	}

	@Required
	public void setFilesToFilter(final Set<String> filesToFilter)
	{
		this.filesToFilter = filesToFilter;
	}
}
