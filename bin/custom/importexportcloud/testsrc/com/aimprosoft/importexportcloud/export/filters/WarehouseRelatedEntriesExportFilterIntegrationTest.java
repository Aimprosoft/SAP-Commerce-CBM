package com.aimprosoft.importexportcloud.export.filters;

import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.export.filters.impl.WarehouseRelatedEntriesExportFilter;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import org.junit.Assert;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Path;


public class WarehouseRelatedEntriesExportFilterIntegrationTest extends AbstractExportFileFilterIntegrationTest
{
	private static final String VENDOR_CSV = "Vendor.csv";

	private String filteredCsv = "Warehouse.csv";

	@Resource
	private WarehouseRelatedEntriesExportFilter warehouseRelatedEntriesExportFilter;

	@Override
	protected void doFilter(final ExportTaskInfoModel exportTaskInfoModel, final Path tempFile) throws ExportException
	{
		warehouseRelatedEntriesExportFilter.filter(exportTaskInfoModel, tempFile);
	}

	@Override
	protected void checkAssertions(final Path tempFile, final ExportTaskInfoModel exportTaskInfoModel) throws IOException
	{
		Assert.assertEquals(2, getFileLines(tempFile, filteredCsv));
		Assert.assertEquals(2, getFileLines(tempFile, VENDOR_CSV));
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
