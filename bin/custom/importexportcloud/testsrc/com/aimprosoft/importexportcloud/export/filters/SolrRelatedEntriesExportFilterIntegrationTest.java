package com.aimprosoft.importexportcloud.export.filters;

import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.export.filters.impl.SolrRelatedEntriesExportFilter;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import org.junit.Assert;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Path;


public class SolrRelatedEntriesExportFilterIntegrationTest extends AbstractExportFileFilterIntegrationTest
{
	private static final String SOLR_FACET_SEARCH_CONFIG = "SolrFacetSearchConfig.csv";
	private static final String SOLR_VALUE_RANGE = "SolrValueRangeSet.csv";

	private String filteredCsv = "SolrIndexedType.csv";

	@Resource
	private SolrRelatedEntriesExportFilter solrRelatedEntriesExportFilter;

	@Override
	protected void doFilter(final ExportTaskInfoModel exportTaskInfoModel, final Path tempFile) throws ExportException
	{
		solrRelatedEntriesExportFilter.filter(exportTaskInfoModel, tempFile);
	}

	@Override
	protected void checkAssertions(final Path tempFile, final ExportTaskInfoModel exportTaskInfoModel) throws IOException
	{
		Assert.assertEquals(2, getFileLines(tempFile, SOLR_FACET_SEARCH_CONFIG));
		Assert.assertEquals(2, getFileLines(tempFile, SOLR_VALUE_RANGE));
		Assert.assertEquals(2, getFileLines(tempFile, filteredCsv));
	}

	public String getFilteredCsv()
	{
		return filteredCsv;
	}

	public void setFilteredCsv(String filteredCsv)
	{
		this.filteredCsv = filteredCsv;
	}
}
