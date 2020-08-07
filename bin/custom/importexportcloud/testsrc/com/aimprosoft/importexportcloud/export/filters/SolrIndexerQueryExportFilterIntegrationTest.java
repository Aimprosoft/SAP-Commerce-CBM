package com.aimprosoft.importexportcloud.export.filters;

import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.export.filters.impl.SolrIndexerQueryExportFilter;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import org.junit.Assert;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Path;


public class SolrIndexerQueryExportFilterIntegrationTest extends AbstractExportFileFilterIntegrationTest
{

	private String filteredCsv = "SolrIndexerQuery.csv";

	@Resource
	private SolrIndexerQueryExportFilter solrIndexerQueryExportFilter;

	@Override
	protected void doFilter(final ExportTaskInfoModel exportTaskInfoModel, final Path tempFile) throws ExportException
	{
		solrIndexerQueryExportFilter.filter(exportTaskInfoModel, tempFile);
	}

	@Override
	protected void checkAssertions(final Path tempFile, final ExportTaskInfoModel exportTaskInfoModel) throws IOException
	{
		Assert.assertEquals(3, getFileLines(tempFile, filteredCsv));
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
