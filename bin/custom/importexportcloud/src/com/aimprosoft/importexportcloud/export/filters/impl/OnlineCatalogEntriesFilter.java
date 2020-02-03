package com.aimprosoft.importexportcloud.export.filters.impl;

import static com.aimprosoft.importexportcloud.constants.ImportexportcloudConstants.ROOT_FOLDER;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.export.filters.ExportFileFilter;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;


public class OnlineCatalogEntriesFilter implements ExportFileFilter
{
	private String onlineCatalogVersionName;

	private Set<String> filesToSkip;

	private Set<String> filesToFilter;

	@Override
	public void filter(final ExportTaskInfoModel exportTaskInfoModel, final Path exportImpexAndCSVsFilePath) throws ExportException
	{
		try (final FileSystem zipFileSystem = FileSystems.newFileSystem(exportImpexAndCSVsFilePath, null))
		{
			final Path root = zipFileSystem.getPath(ROOT_FOLDER);

			Files.walkFileTree(root, new SimpleFileVisitor<Path>()
			{
				@Override
				public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
				{
					try (final Stream<String> lines = Files.lines(file))
					{
						final String csvFileName = file.getFileName().toString();

						if (filesToSkip.contains(csvFileName))
						{
							return FileVisitResult.CONTINUE;
						}

						List<String> filteredLines = lines.filter(line -> !line.contains(onlineCatalogVersionName))
								.collect(Collectors.toList());

						if (filesToFilter.contains(csvFileName))
						{
							filteredLines = filterCatalogUnawareMedia(filteredLines);
						}

						Files.write(file, filteredLines, StandardOpenOption.CREATE);

						return FileVisitResult.CONTINUE;
					}
				}
			});
		}
		catch (final IOException e)
		{
			throw new ExportException("An error occurred while cleaning csv from unnecessary Online content.", e);
		}
	}

	private List<String> filterCatalogUnawareMedia(final List<String> lines)
	{
		return lines.stream().filter(line -> StringUtils.containsAny(line, "siteMapTemplate.vm", "#"))
				.collect(Collectors.toList());
	}

	public Set<String> getFilesToSkip()
	{
		return filesToSkip;
	}

	@Required
	public void setFilesToSkip(final Set<String> filesToSkip)
	{
		this.filesToSkip = filesToSkip;
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

	public String getOnlineCatalogVersionName()
	{
		return onlineCatalogVersionName;
	}

	@Required
	public void setOnlineCatalogVersionName(final String onlineCatalogVersionName)
	{
		this.onlineCatalogVersionName = onlineCatalogVersionName;
	}
}
