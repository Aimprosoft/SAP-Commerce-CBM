package com.aimprosoft.importexportcloud.export.filters;

import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.export.filters.impl.CatalogsFileExportFilter;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import org.junit.Assert;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;


public class CatalogsFileExportFilterIntegrationTest extends AbstractExportFileFilterIntegrationTest
{
	private String filteredCsv = "Media.csv";

	@Resource
	private CatalogsFileExportFilter catalogsFileExportFilter;

	@Override
	protected void doFilter(final ExportTaskInfoModel exportTaskInfoModel, final Path tempFile) throws ExportException, IOException
	{
		try (final FileSystem zipFileSystem = FileSystems.newFileSystem(tempFile, null))
		{
			final Path targetFile = zipFileSystem.getPath(filteredCsv);

			catalogsFileExportFilter.filter(exportTaskInfoModel, targetFile);
		}
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
