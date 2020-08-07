package com.aimprosoft.importexportcloud.export.filters;

import com.aimprosoft.importexportcloud.enums.TaskInfoScope;
import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.export.filters.impl.ClassificationCatalogRelationExportFilter;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.catalog.model.classification.ClassificationSystemModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;


public class ClassificationCatalogRelationExportFilterIntegrationTest extends AbstractExportFileFilterIntegrationTest
{
	private String filteredCsv = "CategoryCategoryRelation.csv";

	@Resource
	private ModelService modelService;

	@Resource
	private ClassificationCatalogRelationExportFilter classificationCatalogRelationExportFilter;

	@Override
	protected void checkAssertions(final Path tempFile, final ExportTaskInfoModel exportTaskInfoModel) throws IOException
	{
		Assert.assertEquals(2, getFileLines(tempFile, filteredCsv));
	}

	@Override
	protected void doFilter(final ExportTaskInfoModel exportTaskInfoModel, final Path tempFile) throws IOException, ExportException
	{
		try (final FileSystem zipFileSystem = FileSystems.newFileSystem(tempFile, null))
		{
			final Path targetFile = zipFileSystem.getPath(filteredCsv);

			classificationCatalogRelationExportFilter.filter(exportTaskInfoModel, targetFile);
		}
	}

	@Test
	public void isApplicable()
	{
		final ExportTaskInfoModel exportTaskInfoModel = modelService.create(ExportTaskInfoModel.class);
		final CatalogVersionModel catalogVersionModel = modelService.create(CatalogVersionModel.class);
		final ClassificationSystemModel classificationSystemModel = modelService.create(ClassificationSystemModel.class);

		catalogVersionModel.setCatalog(classificationSystemModel);
		exportTaskInfoModel.setTaskScope(TaskInfoScope.CATALOGSCOPE);
		exportTaskInfoModel.setCatalogVersion(catalogVersionModel);

		Assert.assertTrue(classificationCatalogRelationExportFilter.isApplicable(exportTaskInfoModel));
	}

	@Test
	public void isNotApplicable()
	{
		final ExportTaskInfoModel exportTaskInfoModel = getExportTaskInfoModelFromTaskInfoData();

		Assert.assertFalse(classificationCatalogRelationExportFilter.isApplicable(exportTaskInfoModel));
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
