package com.aimprosoft.importexportcloud.export.filters;

import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.export.filters.impl.CatalogRelatedEntriesExportFilter;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import org.junit.Assert;
import org.junit.Ignore;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Path;


public class CatalogRelatedEntriesExportFilterIntegrationTest extends AbstractExportFileFilterIntegrationTest
{
	private String filteredCsv = "Media.csv";

	@Resource
	private CatalogRelatedEntriesExportFilter catalogRelatedEntriesExportFilter;

	@Override
	@Ignore
	public void fileToFilterIsNotExistTest()
	{
		//In this case, nothing will happen.
	}

	@Override
	protected void doFilter(final ExportTaskInfoModel exportTaskInfoModel, final Path tempFile) throws ExportException
	{
		catalogRelatedEntriesExportFilter.filter(exportTaskInfoModel, tempFile);
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
