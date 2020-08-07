package com.aimprosoft.importexportcloud.service.storage.impl;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.CloudStorageFacade;
import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.data.CloudObjectData;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.service.storage.StorageService;
import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Random;

import static com.aimprosoft.importexportcloud.constants.ImportexportcloudConstants.ROOT_FOLDER;
import static org.junit.Assert.*;


@IntegrationTest
public class S3StorageServiceIntegrationTest extends ServicelayerTransactionalTest {

    private static final String TEST_STORAGE_CONFIG_CODE = "testStorageConfig";
    private static final String TEST_FOLDER = "IntegrationTestsFolder/";
    private static final String FILE_NAME_PREFIX = "test_file_";
    private static final String FILE_NAME_SUFFIX = ".zip";

    private Path tempFile;
    private StorageConfigData storageConfigData;
    private String region;
    private String appKey;
    private String appSecret;

    @Resource
    StorageConfigFacade storageConfigFacade;

    @Resource
    CloudStorageFacade cloudStorageFacade;

    @Resource
    private StorageService awSs3StorageService;

    @Before
    public void setUp() throws Exception
    {
        createCoreData();
        importCsv("/test/test-user.impex", "UTF-8");
        importCsv("/test/test-storage-configs.impex", "UTF-8");
        storageConfigData = storageConfigFacade.getStorageConfigData(TEST_STORAGE_CONFIG_CODE);
        region = storageConfigData.getRegion();
        appKey = storageConfigData.getAppKey();
        appSecret = storageConfigData.getEncodedAppSecret();
    }

    @After
    public void tearDown() throws Exception
    {
        if (tempFile != null)
        {
            Files.deleteIfExists(tempFile);
        }
    }

    /*
     *   Config validation tests
     */

    @Test(expected = CloudStorageException.class)
    public void testStorageConfigValidationNullRegion() throws CloudStorageException
    {
        StorageConfigData storageConfigData = getStorageConfig(null, appKey, appSecret);
        cloudStorageFacade.connect(storageConfigData);
    }

    @Test(expected = CloudStorageException.class)
    public void testStorageConfigValidationEmptyAppKey() throws CloudStorageException
    {
        StorageConfigData storageConfigData = getStorageConfig(region, "", appSecret);
        cloudStorageFacade.connect(storageConfigData);
    }

    /*
     * Connection tests (something's wrong)
     */

    @Test(expected = CloudStorageException.class)
    public void testConnectWrongRegion() throws CloudStorageException
    {
        StorageConfigData storageConfigData = getStorageConfig("wrong_region", appKey, appSecret);
        cloudStorageFacade.connect(storageConfigData);
    }

    @Test(expected = CloudStorageException.class)
    public void testConnectWrongAppKey() throws CloudStorageException
    {
        StorageConfigData storageConfigData = getStorageConfig(region, "wrong_app_key", appSecret);
        cloudStorageFacade.connect(storageConfigData);
    }

    @Test(expected = CloudStorageException.class)
    public void testConnectWrongAppSecret() throws CloudStorageException
    {
        StorageConfigData storageConfigData = getStorageConfig(region, appKey, "wrong_app_secret");
        cloudStorageFacade.connect(storageConfigData);
    }

    @Test(expected = CloudStorageException.class)
    public void testConnectRegionIsNull() throws CloudStorageException
    {
        StorageConfigData storageConfigData = getStorageConfig(null, appKey, appSecret);
        cloudStorageFacade.connect(storageConfigData);
    }

    @Test(expected = CloudStorageException.class)
    public void testConnectAppSecretIsNull() throws CloudStorageException
    {
        StorageConfigData storageConfigData = getStorageConfig(region, appKey, null);
        cloudStorageFacade.connect(storageConfigData);
    }

    @Test(expected = CloudStorageException.class)
    public void testConnectAppKeyIsNull() throws CloudStorageException
    {
        StorageConfigData storageConfigData = getStorageConfig(region, null, appSecret);
        cloudStorageFacade.connect(storageConfigData);
    }

    /*
     * Connection tests (everything's alright)
     */
    @Test
    public void testConnect() throws CloudStorageException
    {
        cloudStorageFacade.connect(storageConfigData);
    }

    /*
     *   Test upload and download (everything's alright)
     */
    @Test
    public void testUploadAndDownloadIntoFolder() throws CloudStorageException, IOException
    {
        doCheck(TEST_FOLDER);
    }

    @Test
    public void testUploadAndDownloadIntoBucketRoot() throws CloudStorageException, IOException
    {
        doCheck(ROOT_FOLDER);
    }

    private void doCheck(String rootFolder) throws IOException, CloudStorageException
    {
        tempFile = Files.createTempFile("test-", ".tmp");
        final  int fileSizeInBytes = 1000;
        byte[] outputByteArray = getRandomByteArray(fileSizeInBytes);
        ByteArrayInputStream uploadInputStream = new ByteArrayInputStream(outputByteArray);
        Files.copy(uploadInputStream,tempFile, StandardCopyOption.REPLACE_EXISTING);

        TaskInfoData taskInfoData = getTaskInfoData();
        taskInfoData.setFileToUploadPath(tempFile);

        String realFileName = FILE_NAME_PREFIX + System.currentTimeMillis() + FILE_NAME_SUFFIX;
        taskInfoData.setRealFileName(realFileName);

        taskInfoData.setCloudUploadFolderPath(rootFolder);
        awSs3StorageService.upload(taskInfoData);

        taskInfoData.setCloudFileDownloadPath(
        		ROOT_FOLDER.equals(rootFolder)
				  ? realFileName
              : rootFolder + realFileName
		  );

        taskInfoData = awSs3StorageService.download(taskInfoData);

        Path downloadedFilePath = taskInfoData.getDownloadedFilePath();

        assertNotNull(downloadedFilePath);
        assertArrayEquals(outputByteArray, IOUtils.toByteArray(Files.newInputStream(downloadedFilePath)));
    }

    /*
     * Download tests (something's wrong)
     */

    @Test(expected = CloudStorageException.class)
    public void testDownloadEmptyPath() throws CloudStorageException
    {
        TaskInfoData taskInfoData = getTaskInfoData();
        taskInfoData.setCloudFileDownloadPath("");

        awSs3StorageService.download(taskInfoData);
    }

    @Test(expected = CloudStorageException.class)
    public void testDownloadPathIsNotAssigned() throws CloudStorageException
    {
        TaskInfoData taskInfoData = getTaskInfoData();
        awSs3StorageService.download(taskInfoData);
    }

    @Test(expected = CloudStorageException.class)
    public void testDownloadWrongBucket() throws CloudStorageException
    {
        TaskInfoData taskInfoData = getTaskInfoData();
        taskInfoData.getConfig().setBucketName("wrong_bucket^)");
        taskInfoData.setCloudFileDownloadPath("some_file");

        awSs3StorageService.download(taskInfoData);
    }

    @Test(expected = CloudStorageException.class)
    public void testDownloadNoSuchBucket() throws CloudStorageException
    {
        TaskInfoData taskInfoData = getTaskInfoData();
        taskInfoData.getConfig().setBucketName("no_such_bucket");
        taskInfoData.setCloudFileDownloadPath("some_file.txt");

        awSs3StorageService.download(taskInfoData);
    }

    @Test(expected = CloudStorageException.class)
    public void testDownloadWrongKey() throws CloudStorageException
    {
        TaskInfoData taskInfoData = getTaskInfoData();
        taskInfoData.setCloudFileDownloadPath("no_such_file.txt");

        awSs3StorageService.download(taskInfoData);
    }

    /*
     *   Upload tests (something's wrong)
     */

    @Test(expected = CloudStorageException.class)
    public void testUploadWrongBucket() throws CloudStorageException, IOException
    {
        TaskInfoData taskInfoData = getTaskInfoData();

        taskInfoData.getConfig().setBucketName("wrong_bucket^)");
        taskInfoData.setCloudUploadFolderPath("ROOT_FOLDER");
        tempFile = Files.createTempFile("test-", ".tmp");
        taskInfoData.setFileToUploadPath(tempFile);

        awSs3StorageService.upload(taskInfoData);
    }

    @Test(expected = CloudStorageException.class)
    public void testUploadNoSuchBucket() throws CloudStorageException, IOException
    {
        TaskInfoData taskInfoData = getTaskInfoData();

        taskInfoData.setCloudUploadFolderPath("");
        taskInfoData.getConfig().setBucketName("no_such_bucket");
        tempFile = Files.createTempFile("test-", ".tmp");
        taskInfoData.setFileToUploadPath(tempFile);
        taskInfoData.setRealFileName("realFileName");

        awSs3StorageService.upload(taskInfoData);
    }

    /*
     * Test list files
     */

    @Test(expected = CloudStorageException.class)
    public void testListFilesNoSuchBucket() throws CloudStorageException, IOException
    {
        TaskInfoData taskInfoData = getTaskInfoData();

        taskInfoData.getConfig().setBucketName("no_such_bucket");
        taskInfoData.setCloudFolderPath("");
        tempFile = Files.createTempFile("test-", ".tmp");
        taskInfoData.setFileToUploadPath(tempFile);

        awSs3StorageService.listFiles(taskInfoData);
    }

    @Test
    public void testListFilesInBucket() throws CloudStorageException
    {
        TaskInfoData taskInfoData = getTaskInfoData();
        taskInfoData.setCloudFolderPath("");
        taskInfoData.setIsExport(Boolean.FALSE);

        Collection<CloudObjectData> collection = awSs3StorageService.listFiles(taskInfoData);

        assertFalse(collection.isEmpty());
    }

    @Test
    public void testListFilesInFolder() throws CloudStorageException
    {
        TaskInfoData taskInfoData = getTaskInfoData();
        taskInfoData.setCloudFolderPath(TEST_FOLDER);
        taskInfoData.setIsExport(Boolean.FALSE);

        Collection<CloudObjectData> collection = awSs3StorageService.listFiles(taskInfoData);

        assertFalse(collection.isEmpty());
    }

    private StorageConfigData getStorageConfig(String region, String appKey, String appSecret)
    {
        StorageConfigData storageConfigData = new StorageConfigData();
        storageConfigData.setRegion(region);
        storageConfigData.setAppKey(appKey);
        storageConfigData.setEncodedAppSecret(appSecret);
        return storageConfigData;
    }

    private TaskInfoData getTaskInfoData()
    {
        TaskInfoData taskInfoData = new TaskInfoData();
        taskInfoData.setConfig(storageConfigData);
        return taskInfoData;
    }

    private byte[] getRandomByteArray(int size)
    {
        byte[] byteArray = new byte[size];
        new Random().nextBytes(byteArray);
        return byteArray;
    }
}
