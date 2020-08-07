package com.aimprosoft.importexportcloud.export.filters;

import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.export.ExportScriptGeneratorService;
import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import com.aimprosoft.importexportcloud.model.TaskInfoModel;
import com.aimprosoft.importexportcloud.service.TaskInfoService;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.impex.model.ImpExMediaModel;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import de.hybris.platform.servicelayer.impex.ExportConfig;
import de.hybris.platform.servicelayer.impex.ExportResult;
import de.hybris.platform.servicelayer.impex.ExportService;
import de.hybris.platform.servicelayer.impex.ImpExResource;
import de.hybris.platform.servicelayer.impex.impl.StreamBasedImpExResource;
import de.hybris.platform.util.CSVConstants;
import de.hybris.platform.util.MediaUtil;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;


public abstract class AbstractExportFileFilterIntegrationTest extends ServicelayerTransactionalTest
{
	protected static final String CATALOG_VERSION = "testcatalog:Staged";
	protected static final String CMS_SITE_UID = "testsite";
	private static final String DROP_BOX_TEST_EXPORT_CONFIG = "testDropBoxStorageConfig";

	@Resource
	private StorageConfigFacade storageConfigFacade;

	@Resource
	private CMSSiteService cmsSiteService;

	@Resource
	private TaskInfoService<TaskInfoModel> taskInfoService;

	@Resource
	private ExportScriptGeneratorService exportScriptGeneratorService;

	@Resource
	private ExportService exportService;

	@Before
	public void setUp() throws Exception
	{
		createCoreData();
		createDefaultCatalog();
		importCsv("/test/test-user.impex", "UTF-8");
		importCsv("/test/test-storage-configs.impex", "UTF-8");
		importCsv("/test/test-warehouse-vendor-items.impex", "UTF-8");
		importCsv("/test/test-cms-catalog-site.impex", "UTF-8");
		importCsv("/test/test-solr-config.impex", "UTF-8");
		importCsv("/test/test-media.impex", "UTF-8");
	}

	@Test
	public void testFilter() throws ExportException, IOException
	{
		final Path tempFile = getTestDataZip();
		try
		{
			final ExportTaskInfoModel exportTaskInfoModel = getExportTaskInfoModelWithSite();

			doFilter(exportTaskInfoModel, tempFile);
			checkAssertions(tempFile, exportTaskInfoModel);
		}
		finally
		{
			Files.delete(tempFile);
		}
	}

	protected abstract void checkAssertions(final Path tempFile, final ExportTaskInfoModel exportTaskInfoModel)
			throws IOException, ExportException;

	protected abstract void doFilter(final ExportTaskInfoModel exportTaskInfoModel, final Path tempFile)
			throws ExportException, IOException;

	public void fileToFilterIsNotExistTest() throws IOException
	{
		final Path tempFile = getTestDataZip();
		try
		{
			final ExportTaskInfoModel exportTaskInfoModel = getExportTaskInfoModelFromTaskInfoData();
			removeFileToFilter(tempFile);

			Throwable thrown = catchThrowable(() -> doFilter(exportTaskInfoModel, tempFile));
			assertThat(thrown).isInstanceOf(ExportException.class);
		}
		finally
		{
			Files.delete(tempFile);
		}
	}

	@Test(expected = FileSystemNotFoundException.class)
	public void filterWithWrongPath() throws ExportException, IOException
	{
		final String pathValue = "mock";
		final ExportTaskInfoModel exportTaskInfoModel = getExportTaskInfoModelFromTaskInfoData();

		doFilter(exportTaskInfoModel, Paths.get(pathValue));
	}

	protected void removeFileToFilter(final Path exportedArchive) throws IOException
	{
		try (final FileSystem zipFileSystem = FileSystems.newFileSystem(exportedArchive, null))
		{
			final Path targetFile = zipFileSystem.getPath(getFilteredCsv());
			Files.delete(targetFile);
		}
	}

	protected TaskInfoData initTaskInfoData(final String config)
	{
		StorageConfigData storageConfigData;

		storageConfigData = storageConfigFacade.getStorageConfigData(config);
		final TaskInfoData taskInfoData = new TaskInfoData();
		taskInfoData.setConfig(storageConfigData);

		taskInfoData.setTaskInfoScopeCode("SiteScope");
		taskInfoData.setCatalogIdAndVersionName(CATALOG_VERSION);
		taskInfoData.setCmsSiteUid(CMS_SITE_UID);

		return taskInfoData;
	}

	protected Path getTestDataZip()
	{
		final ExportTaskInfoModel exportTaskInfoModel = getExportTaskInfoModelFromTaskInfoData();
		final ExportConfig exportConfig = createExportConfig(exportTaskInfoModel);

		final ExportResult exportResult = exportService.exportData(exportConfig);
		final ImpExMediaModel exportedData = exportResult.getExportedData();

		return Paths.get(MediaUtil.getLocalStorageDataDir() + File.separator + exportedData.getLocation());
	}

	protected CMSSiteModel getTestCMSSiteModel()
	{
		return cmsSiteService.getSites().stream()
				.filter(cmsSiteModel -> cmsSiteModel.getUid().equals(CMS_SITE_UID))
				.findAny()
				.orElse(null);
	}

	protected ExportTaskInfoModel getExportTaskInfoModelWithSite()
	{
		final ExportTaskInfoModel exportTaskInfoModel = getExportTaskInfoModelFromTaskInfoData();
		exportTaskInfoModel.setSite(getTestCMSSiteModel());

		return exportTaskInfoModel;
	}

	protected ExportTaskInfoModel getExportTaskInfoModelFromTaskInfoData()
	{
		final TaskInfoData taskInfoData = initTaskInfoData(DROP_BOX_TEST_EXPORT_CONFIG);
		taskInfoData.setCloudUploadFolderPathToDisplay("TestFolder");

		return taskInfoService.createExportTaskInfoModel(taskInfoData);
	}

	protected int getFileLines(final Path tempFile, final String fileName) throws IOException
	{
		try (final FileSystem zipFileSystem = FileSystems.newFileSystem(tempFile, null))
		{
			final Path targetFile = zipFileSystem.getPath(fileName);
			try (Stream<String> streamOfLines = Files.lines(targetFile))
			{
				final List<String> lines = streamOfLines.collect(Collectors.toList());

				return lines.size();
			}
		}
	}

	private ExportConfig createExportConfig(final ExportTaskInfoModel exportTaskInfoModel)
	{
		final ExportConfig exportConfig = new ExportConfig();

		final String script = exportScriptGeneratorService.generateExportScript(exportTaskInfoModel);
		final ImpExResource resource = new StreamBasedImpExResource(new ByteArrayInputStream(script.getBytes()),
				CSVConstants.HYBRIS_ENCODING, CSVConstants.DEFAULT_FIELD_SEPARATOR);

		exportConfig.setScript(resource);
		exportConfig.setValidationMode(ExportConfig.ValidationMode.STRICT);

		return exportConfig;
	}

	protected abstract String getFilteredCsv();
}
