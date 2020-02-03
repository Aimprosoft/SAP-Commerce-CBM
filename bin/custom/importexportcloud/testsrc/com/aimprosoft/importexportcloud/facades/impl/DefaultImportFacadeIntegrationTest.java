package com.aimprosoft.importexportcloud.facades.impl;

import com.aimprosoft.importexportcloud.exceptions.IemException;
import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.ImportTaskInfoModel;
import com.aimprosoft.importexportcloud.model.TaskInfoModel;
import com.aimprosoft.importexportcloud.service.TaskInfoService;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;


public class DefaultImportFacadeIntegrationTest extends ServicelayerTransactionalTest
{
	@Resource
	private DefaultImportFacade defaultImportFacade;

	@Resource
	private DefaultCloudStorageFacade defaultCloudStorageFacade;

	@Resource
	private StorageConfigFacade storageConfigFacade;

	@Resource
	private TaskInfoService<TaskInfoModel> taskInfoService;

	private static final String DROPBOX_TEST_STORAGE_CONFIG_CODE = "testDropBoxStorageConfig";
	private static final String AWS_TEST_STORAGE_CONFIG_CODE = "testStorageConfig";

	private StorageConfigData storageConfigData;

	@Before
	public void setUp() throws Exception
	{
		createCoreData();
		importCsv("/test/test-user.impex", "UTF-8");
		importCsv("/test/test-storage-configs.impex", "UTF-8");
		storageConfigData = storageConfigFacade.getStorageConfigData(DROPBOX_TEST_STORAGE_CONFIG_CODE);
	}

	@Test
	public void importData() throws IemException
	{
		TaskInfoData taskInfoData = initDTO(DROPBOX_TEST_STORAGE_CONFIG_CODE);
		taskInfoData.setConfig(storageConfigData);

		ImportTaskInfoModel importTaskInfoModel = taskInfoService.createImportTaskInfoModel(taskInfoData);
		taskInfoData.setTaskInfoCode(importTaskInfoModel.getCode());

		Path downloadedFilePath = Paths.get(getClass().getResource("/test/mock.zip").getPath());
		taskInfoData.setDownloadedFilePath(downloadedFilePath);

		assertNotNull(defaultImportFacade.importData(taskInfoData));
	}

	@Test(expected = IemException.class)
	public void importDataNoImpexFile() throws IemException
	{
		TaskInfoData taskInfoData = initDTO(DROPBOX_TEST_STORAGE_CONFIG_CODE);
		taskInfoData.setCloudFileDownloadPath("/test.text");
		assertNotNull(defaultCloudStorageFacade.download(taskInfoData));
		assertNotNull(defaultImportFacade.importData(taskInfoData));
	}

	@Test(expected = IemException.class)
	public void importDataWrongImpexData() throws IemException
	{
		TaskInfoData taskInfoData = initDTO(DROPBOX_TEST_STORAGE_CONFIG_CODE);
		taskInfoData.setCloudFileDownloadPath("/wrong.impex");
		assertNotNull(defaultCloudStorageFacade.download(taskInfoData));
		assertNotNull(defaultImportFacade.importData(taskInfoData));
	}

	@Test(expected = IemException.class)
	public void importDataAwsWrongFile() throws IemException
	{
		TaskInfoData taskInfoData = initDTO(AWS_TEST_STORAGE_CONFIG_CODE);
		taskInfoData.setCloudFileDownloadPath("hybris-importexportcloud-plugin:wrong.zip");
		assertNotNull(defaultCloudStorageFacade.download(taskInfoData));
		assertNotNull(defaultImportFacade.importData(taskInfoData));
	}

	@Test(expected = IemException.class)
	public void importEmptyDataAws() throws IemException
	{
		TaskInfoData taskInfoData = new TaskInfoData();
		taskInfoData.setConfig(storageConfigData);
		taskInfoData.setCloudFileDownloadPath("hybris-importexportcloud-plugin:impex.zip");
		assertNotNull(defaultCloudStorageFacade.download(taskInfoData));
		assertNotNull(defaultImportFacade.importData(taskInfoData));
	}

	private TaskInfoData initDTO(String storageConfig)
	{
		storageConfigData = storageConfigFacade.getStorageConfigData(storageConfig);
		TaskInfoData taskInfoData = new TaskInfoData();
		taskInfoData.setConfig(storageConfigData);
		return taskInfoData;
	}
}
