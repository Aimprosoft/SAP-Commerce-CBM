package com.aimprosoft.importexportcloud.export.filters.impl;

import de.hybris.platform.acceleratorservices.model.cms2.pages.DocumentPageTemplateModel;
import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageTemplateModel;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.cms2.model.contents.ContentCatalogModel;
import de.hybris.platform.cms2.model.pages.PageTemplateModel;
import de.hybris.platform.cms2.servicelayer.daos.CMSPageTemplateDao;
import de.hybris.platform.commons.model.renderer.RendererTemplateModel;
import de.hybris.platform.util.CSVReader;
import de.hybris.platform.util.Config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;


public class RendererTemplateExportFilter extends AbstractSiteRelatedExportFilter
{
	private static final Logger LOG = Logger.getLogger(RendererTemplateExportFilter.class);
	private Set<String> filesToFilter;
	private CMSPageTemplateDao cmsPageTemplateDao;

	@Override
	public void filter(final ExportTaskInfoModel exportTaskInfoModel, final Path exportImpexAndCSVsFilePath) throws ExportException
	{
		final List<String> criteria = getFilteredCriteria(exportTaskInfoModel);

		try (final FileSystem zipFileSystem = FileSystems.newFileSystem(exportImpexAndCSVsFilePath, null))
		{
			for (final String fileName : filesToFilter)
			{
				final Path targetFilePath = zipFileSystem.getPath(fileName);
				try (Stream<String> streamOfLines = Files.lines(targetFilePath))
				{
					final List<String> allLines = streamOfLines.collect(Collectors.toList());

					final List<String> filteredLines = new ArrayList<>();
					filteredLines.add(allLines.get(0)); //header of impex file

					final List<String> codesOfRenderTemplate = new ArrayList<>();
					fillCodesOfRendererTemplate(targetFilePath, codesOfRenderTemplate);
					fillFilteredLines(criteria, codesOfRenderTemplate, allLines, filteredLines);
					logDebug(LOG, "Lines have been filtered. Target file path: %s, codes of renderer templates have been filled", targetFilePath.toString());

					copyIntoTargetZip(filteredLines, targetFilePath);
				}
			}
		}
		catch (IOException e)
		{
			throw new ExportException("An error occurred while cleaning archive", e);
		}
	}

	private List<String> getFilteredCriteria(final ExportTaskInfoModel exportTaskInfoModel)
	{
		final RendererTemplateModel siteMapTemplate = exportTaskInfoModel.getSite().getSiteMapConfig().getSiteMapTemplate();
		final String codeOfSiteMapTemplate = siteMapTemplate.getCode();
		final List<String> criteria = new ArrayList<>();
		criteria.add(codeOfSiteMapTemplate);

		final List<ContentCatalogModel> contentCatalogs = exportTaskInfoModel.getSite().getContentCatalogs();
		Collection<PageTemplateModel> allContentPages = new ArrayList<>();

		final String versionOfCatalog = Config.getString("staged.catalog.version.name", "Staged");
		logDebug(LOG, "Getting filtered criteria for catalog version: %s", versionOfCatalog);

		contentCatalogs.stream()
				.map(CatalogModel::getCatalogVersions)
				.flatMap(Collection::stream)
				.filter(catalogVersion -> catalogVersion.getVersion().equals(versionOfCatalog))
				.findFirst()
				.ifPresent(catalogVersion -> allContentPages
						.addAll(cmsPageTemplateDao.findAllPageTemplatesByCatalogVersion(catalogVersion)));

		final List<String> rendererTemplatesEmailPage = allContentPages.stream()
				.filter(contentPage -> contentPage instanceof EmailPageTemplateModel)
				.map(emailPage -> ((EmailPageTemplateModel) emailPage).getSubject())
				.map(RendererTemplateModel::getCode)
				.collect(Collectors.toList());

		final List<String> rendererTemplatesDocumentPage = allContentPages.stream()
				.filter(contentPage -> contentPage instanceof DocumentPageTemplateModel)
				.map(emailPage -> ((DocumentPageTemplateModel) emailPage).getHtmlTemplate())
				.map(RendererTemplateModel::getCode)
				.collect(Collectors.toList());

		logDebug(LOG, "Codes for rendererTemplatesDocumentPage and rendererTemplatesEmailPage have been got");
		criteria.addAll(rendererTemplatesDocumentPage);
		criteria.addAll(rendererTemplatesEmailPage);
		return criteria;
	}

	private void fillFilteredLines(final List<String> criteria,
			final List<String> codesOfRenderTemplate,
			final List<String> allLines,
			final List<String> filteredLines)
	{
		boolean addFlag = false;
		for (final String line : allLines)
		{
			final boolean isContainsCodeOfEntry = codesOfRenderTemplate.stream().anyMatch(line::contains);
			if (isContainsCodeOfEntry)
			{
				addFlag = criteria.stream().anyMatch(line::contains);
			}
			if (addFlag)
			{
				filteredLines.add(line);
			}
		}
	}


	/**
	 * gets all codes of entries from target file to check
	 * which strings should be added to filtered lines
	 *
	 * @param targetFile            the file to read from
	 * @param codesOfRenderTemplate codes of all entries from file
	 */
	private void fillCodesOfRendererTemplate(final Path targetFile, final List<String> codesOfRenderTemplate)
			throws IOException, ExportException
	{
		final byte[] bytes = Files.readAllBytes(targetFile);
		try (InputStream inputStream = new ByteArrayInputStream(bytes))
		{
			final CSVReader reader = new CSVReader(inputStream, "UTF-8");

			while (reader.readNextLine())
			{
				codesOfRenderTemplate.add(reader.getLine().get(0));
			}
		}
		catch (IOException e)
		{
			throw new ExportException(String.format("An error occurred while reading data from %s", targetFile.toString()), e);
		}
	}

	@Required
	public void setCmsPageTemplateDao(final CMSPageTemplateDao cmsPageTemplateDao)
	{
		this.cmsPageTemplateDao = cmsPageTemplateDao;
	}

	@Required
	public void setFilesToFilter(final Set<String> filesToFilter)
	{
		this.filesToFilter = filesToFilter;
	}
}
