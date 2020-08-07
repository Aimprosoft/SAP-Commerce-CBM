package com.aimprosoft.importexportcloud.export.filters.impl;

import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedPropertyModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrValueRangeSetModel;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class SolrRelatedEntriesExportFilter extends AbstractSiteRelatedExportFilter
{
	private static final Logger LOG = Logger.getLogger(SolrRelatedEntriesExportFilter.class);
	private Set<String> filterBySolrIndexedTypesFiles;
	private Set<String> filterBySolrFacetSearchConfigurationNameFiles;
	private Set<String> filterBySolrValueRangeFiles;

	@Override
	public void filter(final ExportTaskInfoModel exportTaskInfoModel, final Path exportImpexAndCSVsFilePath) throws ExportException
	{
		final List<String> listOfSolrIndexedTypeIdentifier = new ArrayList<>();
		final List<String> listOfSolrFacetSearchConfigName = new ArrayList<>();
		final List<String> listOfValueRangeSetNames = new ArrayList<>();

		createCriteria(exportTaskInfoModel, listOfSolrFacetSearchConfigName, listOfSolrIndexedTypeIdentifier,
				listOfValueRangeSetNames);

		try (final FileSystem zipFileSystem = FileSystems.newFileSystem(exportImpexAndCSVsFilePath, null))
		{
			processFilter(zipFileSystem, listOfSolrFacetSearchConfigName, filterBySolrFacetSearchConfigurationNameFiles);
			processFilter(zipFileSystem, listOfSolrIndexedTypeIdentifier, filterBySolrIndexedTypesFiles);
			processFilter(zipFileSystem, listOfValueRangeSetNames, filterBySolrValueRangeFiles);
		}
		catch (IOException e)
		{
			throw new ExportException("An error occurred while cleaning archive", e);
		}
	}

	private void processFilter(final FileSystem zipFileSystem, final List<String> criteria, final Set<String> setOfFiles)
			throws IOException
	{
		for (final String fileName : setOfFiles)
		{
			final Path targetFilePath = zipFileSystem.getPath(fileName);

			final List<String> filteredLines = getFilteredLines(fileName, zipFileSystem, criteria);
			logDebug(LOG, "Filter processed for file: %s ", targetFilePath.getFileName());
			copyIntoTargetZip(filteredLines, targetFilePath);
		}
		criteria.clear();
	}

	private List<String> getFilteredLines(final String fileName, final FileSystem zipFileSystem, final List<String> criteriaList)
			throws IOException
	{
		final Path targetFilePath = zipFileSystem.getPath(fileName);
		Predicate<String> catalogPredicate = line ->  criteriaList.stream().anyMatch(line::contains);
		final List<String> filteredLines = filterFileLines(targetFilePath, catalogPredicate);

		copyIntoTargetZip(filteredLines, targetFilePath);

		return filteredLines;
	}

	private void createCriteria(final ExportTaskInfoModel exportTaskInfoModel,
			final List<String> listOfSolrFacetSearchConfigName,
			final List<String> listOfSolrIndexedTypeIdentifier,
			final List<String> listOfValueRangeSetNames)
			throws ExportException
	{
		final SolrFacetSearchConfigModel solrFacetSearchConfiguration = exportTaskInfoModel.getSite()
				.getSolrFacetSearchConfiguration();
		if (solrFacetSearchConfiguration != null)
		{
			final List<String> solrFacetSearchConfigurationName = Collections.singletonList(solrFacetSearchConfiguration.getName());

			final List<String> identifiersOfSolrIndexedType = solrFacetSearchConfiguration.getSolrIndexedTypes()
					.stream()
					.map(SolrIndexedTypeModel::getIdentifier)
					.collect(Collectors.toList());

			final List<String> namesOfSolrValueRangeSet = solrFacetSearchConfiguration.getSolrIndexedTypes()
					.stream()
					.map(SolrIndexedTypeModel::getSolrIndexedProperties)
					.flatMap(Collection::stream)
					.map(SolrIndexedPropertyModel::getRangeSets)
					.filter(Objects::nonNull)
					.flatMap(Collection::stream)
					.map(SolrValueRangeSetModel::getName)
					.collect(Collectors.toList());

			listOfSolrFacetSearchConfigName.addAll(solrFacetSearchConfigurationName);
			listOfSolrIndexedTypeIdentifier.addAll(identifiersOfSolrIndexedType);
			listOfValueRangeSetNames.addAll(namesOfSolrValueRangeSet);
		}
		else
		{
			throw new ExportException("Impossible filter these files. Please check solrFacetSearchConfiguration for your site");
		}
	}

	@Required
	public void setFilterBySolrIndexedTypesFiles(final Set<String> filterBySolrIndexedTypesFiles)
	{
		this.filterBySolrIndexedTypesFiles = filterBySolrIndexedTypesFiles;
	}

	@Required
	public void setFilterBySolrFacetSearchConfigurationNameFiles(
			final Set<String> filterBySolrFacetSearchConfigurationNameFiles)
	{
		this.filterBySolrFacetSearchConfigurationNameFiles = filterBySolrFacetSearchConfigurationNameFiles;
	}

	@Required
	public void setFilterBySolrValueRangeFiles(final Set<String> filterBySolrValueRangeFiles)
	{
		this.filterBySolrValueRangeFiles = filterBySolrValueRangeFiles;
	}
}
