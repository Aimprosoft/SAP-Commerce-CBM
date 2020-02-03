package com.aimprosoft.importexportcloud.facades.impl;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertNotNull;

@IntegrationTest
public class DefaultExportFacadeIntegrationTest extends ServicelayerTransactionalTest
{
   @Resource
   private DefaultExportFacade defaultExportFacade;

   @Resource
   private StorageConfigFacade storageConfigFacade;

   @Resource
   private DefaultCloudStorageFacade defaultCloudStorageFacade;

   private static final String DROP_BOX_TEST_EXPORT_CONFIG = "testDropBoxStorageConfig";
   private static final String AWS_TEST_EXPORT_CONFIG = "testStorageConfig";
   private static final String CATALOG_SCOPE = "CatalogScope";
   private static final String CATALOG_VERSION = "testcatalog:Staged";
   private static final String CATALOG_VERSION_WRONG = "wrongcatalog:Staged";
   private static final String CMS_SITE_UID = "testsite";
   private static final String FILE_NAME_PREFIX = "test_export_upload";
   private static final String FILE_NAME_SUFFIX = ".zip";
   private static final String AWS_PATH = "hybris-importexportcloud-plugin:";

   private StorageConfigData storageConfigData;

   @Before
   public void setUp() throws Exception
   {
      createCoreData();
      importCsv("/test/test-user.impex", "UTF-8");
      importCsv("/test/test-storage-configs.impex", "UTF-8");
      importCsv("/test/test-cms-catalog-site.impex", "UTF-8");
   }

   @Test
   public void dropboxExportData() throws ExportException
   {
      TaskInfoData taskInfoData = initTaskInfoData(DROP_BOX_TEST_EXPORT_CONFIG);
      taskInfoData.setCloudUploadFolderPathToDisplay("TestFolder");
      assertNotNull(defaultExportFacade.exportData(taskInfoData));
   }

   @Test(expected = ConversionException.class)
   public void dropboxExportEmptyScope() throws ConversionException, ExportException
   {
      TaskInfoData taskInfoData = initTaskInfoData(DROP_BOX_TEST_EXPORT_CONFIG);
      taskInfoData.setTaskInfoScopeCode("");
      assertNotNull(defaultExportFacade.exportData(taskInfoData));
   }

   @Test(expected = UnknownIdentifierException.class)
   public void dropboxExportWrongCV() throws UnknownIdentifierException, ExportException
   {
      TaskInfoData taskInfoData = initTaskInfoData(DROP_BOX_TEST_EXPORT_CONFIG);
      taskInfoData.setCatalogIdAndVersionName(CATALOG_VERSION_WRONG);
      assertNotNull(defaultExportFacade.exportData(taskInfoData));
   }

   @Test
   public void awsExportData() throws ExportException
   {
      TaskInfoData taskInfoData = initTaskInfoData(AWS_TEST_EXPORT_CONFIG);
      String cloudFilePath = AWS_PATH + FILE_NAME_PREFIX + System.currentTimeMillis() + FILE_NAME_SUFFIX;
      taskInfoData.setCloudUploadFolderPathToDisplay(cloudFilePath);
      assertNotNull(defaultExportFacade.exportData(taskInfoData));
   }

   @Test(expected = ConversionException.class)
   public void awsExportEmptyScope() throws ConversionException, ExportException
   {
      TaskInfoData taskInfoData = initTaskInfoData(AWS_TEST_EXPORT_CONFIG);
      taskInfoData.setTaskInfoScopeCode("");
      assertNotNull(defaultExportFacade.exportData(taskInfoData));
   }

   @Test(expected = UnknownIdentifierException.class)
   public void awsExportWrongCV() throws UnknownIdentifierException, ExportException
	{
      TaskInfoData taskInfoData = initTaskInfoData(AWS_TEST_EXPORT_CONFIG);
      taskInfoData.setCatalogIdAndVersionName(CATALOG_VERSION_WRONG);
      assertNotNull(defaultExportFacade.exportData(taskInfoData));
   }

   @Test
   public void importDataAws() throws CloudStorageException, ExportException
   {
      TaskInfoData taskInfoData = initTaskInfoData(AWS_TEST_EXPORT_CONFIG);
      String cloudFilePath = AWS_PATH + FILE_NAME_PREFIX + System.currentTimeMillis() + FILE_NAME_SUFFIX;
      taskInfoData.setCloudUploadFolderPath(cloudFilePath);
      taskInfoData.setCloudUploadFolderPathToDisplay(cloudFilePath);
      assertNotNull(defaultExportFacade.exportData(taskInfoData));
      assertNotNull(defaultCloudStorageFacade.upload(taskInfoData));
   }

   private TaskInfoData initTaskInfoData(String config)
   {
      storageConfigData = storageConfigFacade.getStorageConfigData(config);
      TaskInfoData taskInfoData = new TaskInfoData();
      taskInfoData.setConfig(storageConfigData);

      taskInfoData.setTaskInfoScopeCode(CATALOG_SCOPE);
      taskInfoData.setCatalogIdAndVersionName(CATALOG_VERSION);
      taskInfoData.setCmsSiteUid(CMS_SITE_UID);
      return taskInfoData;
   }

}
