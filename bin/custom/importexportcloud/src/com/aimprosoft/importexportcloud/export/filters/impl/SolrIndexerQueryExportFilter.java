package com.aimprosoft.importexportcloud.export.filters.impl;

import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class SolrIndexerQueryExportFilter extends AbstractSiteRelatedExportFilter
{
	private static final Logger LOG = Logger.getLogger(SolrIndexerQueryExportFilter.class);
	private static final String EOL = System.lineSeparator();
	private static final String REG_EXP = "([\\S]+SELECT((?!;)[\\S\\s])+?;%s.+[annonymous|admin])";
	private Set<String> filesToFilter;

	@Override
	public void filter(final ExportTaskInfoModel exportTaskInfoModel, final Path exportImpexAndCSVsFilePath) throws ExportException
	{
		final List<String> criteria = getCriteria(exportTaskInfoModel);
		Path targetFilePath = null;
		try (final FileSystem zipFileSystem = FileSystems.newFileSystem(exportImpexAndCSVsFilePath, null))
		{
			for (final String fileName : filesToFilter)
			{
				targetFilePath = zipFileSystem.getPath(fileName);
				final List<String> allLinesOfFile = Files.readAllLines(targetFilePath);
				final StringBuilder lines = getLinesOfFile(allLinesOfFile);

				final List<String> filteredLines = new ArrayList<>();
				filteredLines.add(allLinesOfFile.get(0)); //this is header of impex file
				filteredLines.addAll(getFilteredLines(lines, criteria));
				logDebug(LOG, "Lines have been filtered. Target file path: %s", targetFilePath.toString());

				copyIntoTargetZip(filteredLines, targetFilePath);
			}
		}
		catch (IOException e)
		{
			throw new ExportException(String.format("An error occurred while cleaning %s file.",
					targetFilePath != null ? targetFilePath.getFileName().toString() : null), e);
		}
	}

	private List<String> getCriteria(final ExportTaskInfoModel exportTaskInfoModel)
	{
		final List<SolrIndexedTypeModel> solrIndexedTypes = exportTaskInfoModel.getSite().getSolrFacetSearchConfiguration()
				.getSolrIndexedTypes();

		return solrIndexedTypes.stream()
				.map(SolrIndexedTypeModel::getIdentifier)
				.collect(Collectors.toList());
	}

	private List<String> getFilteredLines(final StringBuilder lines, final List<String> criteria)
	{
		final List<String> filteredLines = new ArrayList<>();

		final String criteries = criteria.stream()
				.collect(Collectors.joining("|"));
		final String regExpForSite = String.format(REG_EXP, criteries);
		final Pattern pattern = Pattern.compile(regExpForSite);
		final Matcher matcher = pattern.matcher(lines);

		while (matcher.find())
		{
			filteredLines.add(matcher.group(0));
		}
		return filteredLines;
	}

	private StringBuilder getLinesOfFile(final List<String> allLinesOfFile)
	{
		final StringBuilder lines = new StringBuilder();
		for (final String line : allLinesOfFile)
		{
			lines.append(line).append(EOL);
		}
		return lines;
	}

	@Required
	public void setFilesToFilter(final Set<String> filesToFilter)
	{
		this.filesToFilter = filesToFilter;
	}
}
