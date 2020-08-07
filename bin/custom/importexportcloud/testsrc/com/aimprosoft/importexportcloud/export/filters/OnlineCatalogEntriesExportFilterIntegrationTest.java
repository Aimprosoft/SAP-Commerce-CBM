package com.aimprosoft.importexportcloud.export.filters;

import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.export.filters.impl.OnlineCatalogEntriesExportFilter;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import org.junit.Assert;
import org.junit.Ignore;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Path;


public class OnlineCatalogEntriesExportFilterIntegrationTest extends AbstractExportFileFilterIntegrationTest
{

	private static final String IGNORED_CSV = "EmailPageTemplate.csv";

	private String filteredCsv = "CatalogUnawareMedia.csv";

	@Resource
	private OnlineCatalogEntriesExportFilter onlineCatalogEntriesExportFilter;

	@Override
	protected void doFilter(final ExportTaskInfoModel exportTaskInfoModel, final Path tempFile) throws ExportException
	{
		onlineCatalogEntriesExportFilter.filter(exportTaskInfoModel, tempFile);
	}

	@Override
	protected void checkAssertions(final Path tempFile, final ExportTaskInfoModel exportTaskInfoModel) throws IOException
	{
		Assert.assertEquals(1, getFileLines(tempFile, filteredCsv));
		Assert.assertEquals(3, getFileLines(tempFile, IGNORED_CSV));
	}

	@Override
	@Ignore
	public void fileToFilterIsNotExistTest()
	{
		//In this case, nothing will happen.
	}

	@Override
	protected String getFilteredCsv()
	{
		return filteredCsv;
	}

	public void setFilteredCsv(String filteredCsv)
	{
		this.filteredCsv = filteredCsv;
	}
}
