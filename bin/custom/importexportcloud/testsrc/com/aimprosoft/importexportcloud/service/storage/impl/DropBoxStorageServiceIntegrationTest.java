package com.aimprosoft.importexportcloud.service.storage.impl;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.CloudStorageFacade;
import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.data.CloudObjectData;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import de.hybris.platform.servicelayer.session.SessionService;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Random;

import static com.aimprosoft.importexportcloud.constants.ImportexportcloudConstants.ROOT_FOLDER;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;


@IntegrationTest
public class DropBoxStorageServiceIntegrationTest extends ServicelayerTransactionalTest
{
	@Resource
	private DropBoxStorageService dropBoxStorageService;

	@Resource
	private CloudStorageFacade cloudStorageFacade;

	@Resource
	private SessionService sessionService;

	@Resource
	private StorageConfigFacade storageConfigFacade;

	private String appKey;
	private String appSecret;
	private String token;

	private static final String TEST_STORAGE_CONFIG_CODE = "testDropBoxStorageConfig";
	private static final String DROPBOX_AUTH_CODE_ATTRIBUTE = "dropBoxAuthCode";
	private static final String DROPBOX_AUTH_CODE = "simpleValid";
	private static final String FILE_NAME_PREFIX = "test_file_";
	private static final String FILE_NAME_SUFFIX = ".zip";
	private static final String DROPBOX_STORAGE_TYPE = "dropBoxStorageType";

	@Before
	public void setUp() throws Exception
	{
		createCoreData();
		importCsv("/test/test-user.impex", "UTF-8");
		importCsv("/test/test-storage-configs.impex", "UTF-8");
		StorageConfigData storageConfigData = storageConfigFacade.getStorageConfigData(TEST_STORAGE_CONFIG_CODE);
		appKey = storageConfigData.getAppKey();
		appSecret = storageConfigData.getEncodedAppSecret();
		token = storageConfigData.getAccessToken();

	}

	@Test
	public void connectWithAuthCode()
	{
		// test is implemented in DropBoxCloudStorageServiceUnitTest
		assertTrue(true);
	}

	@Test(expected = CloudStorageException.class)
	public void connectWithWrongAuthCode() throws CloudStorageException
	{
		StorageConfigData config = getConfig(null, appKey, appSecret);
		cloudStorageFacade.connect(config);
	}

	@Test(expected = CloudStorageException.class)
	public void connectWithEmptyAuthCode() throws CloudStorageException
	{
		StorageConfigData config = new StorageConfigData();
		cloudStorageFacade.connect(config);
	}

	@Test(expected = CloudStorageException.class)
	public void connectWrongCredentials() throws CloudStorageException
	{
		sessionService.setAttribute(DROPBOX_AUTH_CODE_ATTRIBUTE, DROPBOX_AUTH_CODE);
		StorageConfigData config = getConfig(null, "wrong", "wrong");
		cloudStorageFacade.connect(config);
	}

	@Test
	public void testListFiles() throws CloudStorageException
	{
		TaskInfoData taskInfoData = getTaskInfoData(token, appKey, appSecret);
		taskInfoData.setCloudFolderPath("");
		taskInfoData.setIsExport(Boolean.FALSE);
		assertNotNull(dropBoxStorageService.listFiles(taskInfoData));
	}

	@Test(expected = CloudStorageException.class)
	public void wrongPathListFiles() throws CloudStorageException
	{
		TaskInfoData taskInfoData = getTaskInfoData(token, appKey, appSecret);
		taskInfoData.setCloudFolderPath("/wrongPath");
		assertNotNull(dropBoxStorageService.listFiles(taskInfoData));
	}

	@Test
	public void testUploadLisFilesDownload() throws CloudStorageException, IOException
	{
		TaskInfoData taskInfoData = getInitTestFiles();
		dropBoxStorageService.upload(taskInfoData);

		taskInfoData.setCloudFolderPath("");
		taskInfoData.setIsExport(Boolean.FALSE);

		String realFileName = taskInfoData.getRealFileName();
		String cloudFileDownloadPath = getCloudFileDownloadPath(taskInfoData, realFileName);

		taskInfoData.setCloudFileDownloadPath(cloudFileDownloadPath);

		assertNotNull(dropBoxStorageService.download(taskInfoData));
	}

	@Test(expected = CloudStorageException.class)
	public void testDownloadWrongFile() throws CloudStorageException
	{
		TaskInfoData taskInfoData = getTaskInfoData(token, appKey, appSecret);
		taskInfoData.setCloudFileDownloadPath("/wrongFile.txt");
		assertNotNull(dropBoxStorageService.download(taskInfoData));
	}

	@Test
	public void testUpload() throws CloudStorageException, IOException
	{
		TaskInfoData taskInfoData = getInitTestFiles();
		assertNotNull(dropBoxStorageService.upload(taskInfoData));
	}

	@Test(expected = CloudStorageException.class)
	public void testWrongFileUpload() throws CloudStorageException, IOException
	{
		TaskInfoData taskInfoData = getInitTestFiles();
		Path tempFile = Paths.get("wrong");
		taskInfoData.setFileToUploadPath(tempFile);
		assertNotNull(dropBoxStorageService.upload(taskInfoData));
	}

	@Test(expected = CloudStorageException.class)
	public void testWrongPathUpload() throws CloudStorageException, IOException
	{
		TaskInfoData taskInfoData = getInitTestFiles();
		String cloudFilePath = "unreal/folder/test";
		taskInfoData.setCloudUploadFolderPath(cloudFilePath);
		assertNotNull(dropBoxStorageService.upload(taskInfoData));
	}

	private TaskInfoData getTaskInfoData(String token, String appKey, String appSecret)
	{
		StorageConfigData storageConfigData = new StorageConfigData();
		storageConfigData.setAccessToken(token);
		storageConfigData.setAppKey(appKey);
		storageConfigData.setEncodedAppSecret(appSecret);
		StorageTypeData storageTypeData = new StorageTypeData();
		storageTypeData.setCode(DROPBOX_STORAGE_TYPE);
		storageConfigData.setStorageTypeData(storageTypeData);
		TaskInfoData taskInfoData = new TaskInfoData();
		taskInfoData.setConfig(storageConfigData);
		return taskInfoData;
	}

	private StorageConfigData getConfig(String token, String appKey, String appSecret)
	{
		StorageConfigData storageConfigData = new StorageConfigData();
		storageConfigData.setAccessToken(token);
		storageConfigData.setAppKey(appKey);
		storageConfigData.setEncodedAppSecret(appSecret);

		return storageConfigData;
	}

	private TaskInfoData getInitTestFiles() throws IOException
	{
		TaskInfoData taskInfoData = getTaskInfoData(token, appKey, appSecret);
		Path tempFile = Files.createTempFile("test-", ".tmp");
		int FILE_SIZE_IN_BYTES = 1001;
		byte[] outputByteArray = new byte[FILE_SIZE_IN_BYTES];
		new Random().nextBytes(outputByteArray);
		ByteArrayInputStream uploadInputStream = new ByteArrayInputStream(outputByteArray);
		Files.copy(uploadInputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

		String realFileName = FILE_NAME_PREFIX + System.currentTimeMillis() + FILE_NAME_SUFFIX;
		taskInfoData.setCloudUploadFolderPath(ROOT_FOLDER);
		taskInfoData.setRealFileName(realFileName);

		taskInfoData.setFileToUploadPath(tempFile);
		return taskInfoData;
	}

	private String getCloudFileDownloadPath(TaskInfoData taskInfoData, String realFileName) throws CloudStorageException
	{
		String result = null;

		Collection<CloudObjectData> cloudObjectDataCollection = dropBoxStorageService.listFiles(taskInfoData);

		for (CloudObjectData cloudObjectData : cloudObjectDataCollection)
		{
			if (!cloudObjectData.isFolder() && realFileName.equals(cloudObjectData.getTitle()))
			{
				result = cloudObjectData.getName();
			}
		}

		return result;
	}
}
