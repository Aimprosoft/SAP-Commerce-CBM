package com.aimprosoft.importexportcloud.facades.impl;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import de.hybris.platform.servicelayer.session.SessionService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpSession;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Random;

import static org.junit.Assert.*;


@IntegrationTest
public class DefaultCloudStorageFacadeIntegrationTest extends ServicelayerTransactionalTest
{

	@Resource
	private DefaultCloudStorageFacade defaultCloudStorageFacade;

	@Resource
	private SessionService sessionService;

	@Resource
	private StorageConfigFacade storageConfigFacade;

	private static final String DROPBOX_AUTH_CODE_ATTRIBUTE = "dropBoxAuthCode";
	private static final String DROPBOX_AUTH_CODE = "simpleValid";
	private static final String DROPBOX_AUTH2_SUBSTRING = "https://www.dropbox.com/oauth2/authorize?response_type=code&redirect_uri=";
	private static final String AWS_TEST_STORAGE_CONFIG_CODE = "testStorageConfig";
	private static final String DROPBOX_TEST_STORAGE_CONFIG_CODE = "testDropBoxStorageConfig";
	private static final String FILE_NAME_PREFIX = "test_file_";
	private static final String FILE_NAME_SUFFIX = ".txt";

	private StorageConfigData storageConfigData;

	@Before
	public void setUp() throws Exception
	{
		createCoreData();
		createDefaultCatalog();
		importCsv("/test/test-user.impex", "UTF-8");
		importCsv("/test/test-storage-configs.impex", "UTF-8");
	}

	@Test(expected = CloudStorageException.class)
	public void connectWithWrongSessionValue() throws CloudStorageException
	{
		initDTO();
		sessionService.setAttribute(DROPBOX_AUTH_CODE_ATTRIBUTE, null);
		defaultCloudStorageFacade.connect(storageConfigData);
		assertFalse(storageConfigData.getIsConnected());
	}

	@Test(expected = CloudStorageException.class)
	public void connectAWSWithEmptyConfig() throws CloudStorageException
	{
		initDTOaws();
		storageConfigData.setAppKey(null);
		storageConfigData.setEncodedAppSecret("");
		defaultCloudStorageFacade.connect(storageConfigData);
	}

	@Test
	public void connectAWS() throws CloudStorageException
	{
		initDTOaws();
		defaultCloudStorageFacade.connect(storageConfigData);
	}

	@Test
	public void getAuthURL() throws CloudStorageException
	{
		initDTO();
		HttpSession session = new MockHttpSession();
		String authUrl = defaultCloudStorageFacade.getAuthURL(storageConfigData, session);
		assertTrue(authUrl.contains(DROPBOX_AUTH2_SUBSTRING));
	}


	/* You should execute upload methods before testing downloading */
	@Test
	public void uploadDropBox() throws CloudStorageException
	{
		TaskInfoData taskInfoData = getTaskInfoData();

		StorageConfigData storageConfigData = storageConfigFacade.getStorageConfigData(DROPBOX_TEST_STORAGE_CONFIG_CODE);
		taskInfoData.setConfig(storageConfigData);

		defaultCloudStorageFacade.upload(taskInfoData);
	}

	@Test
	public void uploadS3() throws CloudStorageException
	{
		TaskInfoData taskInfoData = getTaskInfoData();

		StorageConfigData storageConfigData = storageConfigFacade.getStorageConfigData(AWS_TEST_STORAGE_CONFIG_CODE);
		taskInfoData.setConfig(storageConfigData);

		defaultCloudStorageFacade.upload(taskInfoData);
	}

	@Test
	public void download() throws CloudStorageException
	{
		TaskInfoData taskInfoData = new TaskInfoData();
		initDTO();
		taskInfoData.setConfig(storageConfigData);
		taskInfoData.setCloudFileDownloadPath("/mock.zip");
		assertNotNull(defaultCloudStorageFacade.download(taskInfoData));
	}

	@Test
	public void downloadS3() throws CloudStorageException
	{
		TaskInfoData taskInfoData = new TaskInfoData();
		initDTOaws();
		taskInfoData.setConfig(storageConfigData);
		taskInfoData.setCloudFileDownloadPath("/mock.zip");
		assertNotNull(defaultCloudStorageFacade.download(taskInfoData));
	}

	@Test(expected = CloudStorageException.class)
	public void downloadAWSWrongPath() throws CloudStorageException
	{
		TaskInfoData taskInfoData = new TaskInfoData();
		initDTOaws();
		taskInfoData.setConfig(storageConfigData);
		taskInfoData.setCloudFileDownloadPath("wrong:impex.zip");
		assertNotNull(defaultCloudStorageFacade.download(taskInfoData));
	}

	@Test(expected = CloudStorageException.class)
	public void downloadAWSWrongFile() throws CloudStorageException
	{
		TaskInfoData taskInfoData = new TaskInfoData();
		initDTOaws();
		taskInfoData.setConfig(storageConfigData);
		taskInfoData.setCloudFileDownloadPath("hybris-importexportcloud-plugin:wrong.zip");
		assertNotNull(defaultCloudStorageFacade.download(taskInfoData));
	}

	@Test(expected = CloudStorageException.class)
	public void downloadNotExistFile() throws CloudStorageException
	{
		TaskInfoData taskInfoData = new TaskInfoData();
		initDTO();
		taskInfoData.setConfig(storageConfigData);

		taskInfoData.setCloudFileDownloadPath("/unreal.txt");
		assertNull(defaultCloudStorageFacade.download(taskInfoData));
	}

	@Test(expected = NullPointerException.class)
	public void downloadWithEmptyConfig() throws CloudStorageException, IllegalArgumentException
	{
		TaskInfoData taskInfoData = new TaskInfoData();
		assertNull(defaultCloudStorageFacade.download(taskInfoData));
	}

	@Test
	public void dropboxUploadTest() throws CloudStorageException, IOException
	{
		TaskInfoData taskInfoData = new TaskInfoData();
		initDTO();
		taskInfoData.setConfig(storageConfigData);
		Path tempFile = Files.createTempFile("test-", ".tmp");
		ByteArrayInputStream uploadInputStream = generateRandomArray();

		Files.copy(uploadInputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
		String cloudFilePath = "/" + FILE_NAME_PREFIX + System.currentTimeMillis() + FILE_NAME_SUFFIX;
		taskInfoData.setCloudUploadFolderPath(cloudFilePath);
		taskInfoData.setFileToUploadPath(tempFile);
		assertNotNull(defaultCloudStorageFacade.upload(taskInfoData));
	}

	private ByteArrayInputStream generateRandomArray()
	{
		int FILE_SIZE_IN_BYTES = 21;
		byte[] outputByteArray = new byte[FILE_SIZE_IN_BYTES];
		new Random().nextBytes(outputByteArray);
		return new ByteArrayInputStream(outputByteArray);
	}

	@Test
	public void listFiles() throws CloudStorageException
	{
		TaskInfoData taskInfoData = new TaskInfoData();
		initDTO();
		taskInfoData.setCloudFolderPath("");
		taskInfoData.setConfig(storageConfigData);
		taskInfoData.setIsExport(Boolean.TRUE);
		assertNotNull(defaultCloudStorageFacade.listFiles(taskInfoData));
	}

	private void initDTO()
	{
		storageConfigData = storageConfigFacade.getStorageConfigData(DROPBOX_TEST_STORAGE_CONFIG_CODE);
		sessionService.setAttribute(DROPBOX_AUTH_CODE_ATTRIBUTE, DROPBOX_AUTH_CODE);
	}

	private void initDTOaws()
	{
		storageConfigData = storageConfigFacade.getStorageConfigData(AWS_TEST_STORAGE_CONFIG_CODE);
	}

	private TaskInfoData getTaskInfoData()
	{
		TaskInfoData taskInfoData = new TaskInfoData();

		taskInfoData.setTaskInfoScopeCode("CatalogScope");
		taskInfoData.setCatalogIdAndVersionName("testCatalog:Online");
		taskInfoData.setExportMediaNeeded(true);
		taskInfoData.setMigrateMediaNeeded(false);
		taskInfoData.setCloudUploadFolderPath("/");
		taskInfoData.setCloudUploadFolderPathToDisplay("/");

		Path fileToUploadPath = Paths.get(getClass().getResource("/test/mock.zip").getPath());
		taskInfoData.setFileToUploadPath(fileToUploadPath);

		taskInfoData.setRealFileName("mock.zip");

		return taskInfoData;
	}
}
