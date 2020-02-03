package com.aimprosoft.importexportcloud.facades.impl;

import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.model.AWSs3StorageConfigModel;
import com.aimprosoft.importexportcloud.model.DropBoxStorageConfigModel;
import com.aimprosoft.importexportcloud.model.StorageTypeModel;
import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.*;


@IntegrationTest
public class DefaultStorageConfigFacadeIntegrationTest extends ServicelayerTransactionalTest
{
	@Resource
	private StorageConfigFacade storageConfigFacade;

	@Resource
	private ModelService modelService;

	@Resource
	private UserService userService;

	private static final String AWS_S3_STORAGE_TYPE_CODE = "aWSs3StorageType";
	private static final String DROP_BOX_STORAGE_TYPE_CODE = "dropBoxStorageType";
	private static final String AWS_S3_STORAGE_CONFIG1_CODE = "aWSs3StorageConfig1";
	private static final String AWS_S_3_STORAGE_CONFIG_2_CODE = "aWSs3StorageConfig2";
	private static final String DROP_BOX_STORAGE_CONFIG_1_CODE = "dropBoxStorageConfig1";
	private UserModel userWithConfigs;
	private UserModel userWithoutConfigs;
	private AWSs3StorageConfigModel aWSs3StorageConfig1;

	@Before
	public void setUp() throws Exception
	{
		createCoreData();
		createDefaultUsers();

		userWithConfigs = userService.getUserForUID("ariel");
		userWithoutConfigs = userService.getUserForUID("demo");

		StorageTypeModel aWSs3StorageType = modelService.create(StorageTypeModel.class);
		aWSs3StorageType.setCode(AWS_S3_STORAGE_TYPE_CODE);
		aWSs3StorageType.setName(AWS_S3_STORAGE_TYPE_CODE);

		StorageTypeModel dropBoxStorageType = modelService.create(StorageTypeModel.class);
		dropBoxStorageType.setCode(DROP_BOX_STORAGE_TYPE_CODE);
		dropBoxStorageType.setName(DROP_BOX_STORAGE_TYPE_CODE);

		aWSs3StorageConfig1 = modelService.create(AWSs3StorageConfigModel.class);
		aWSs3StorageConfig1.setCode(AWS_S3_STORAGE_CONFIG1_CODE);
		aWSs3StorageConfig1.setName(AWS_S3_STORAGE_CONFIG1_CODE);
		aWSs3StorageConfig1.setType(aWSs3StorageType);
		aWSs3StorageConfig1.setUser(userWithConfigs);
		aWSs3StorageConfig1.setAppKey("appKey1");
		aWSs3StorageConfig1.setRegion("region1");
		aWSs3StorageConfig1.setEncodedAppSecret("encodedAppSecret1");
		aWSs3StorageConfig1.setBucketName("bucketName");

		AWSs3StorageConfigModel aWSs3StorageConfig2 = modelService.create(AWSs3StorageConfigModel.class);
		aWSs3StorageConfig2.setCode(AWS_S_3_STORAGE_CONFIG_2_CODE);
		aWSs3StorageConfig2.setName(AWS_S_3_STORAGE_CONFIG_2_CODE);
		aWSs3StorageConfig2.setType(aWSs3StorageType);
		aWSs3StorageConfig2.setUser(userWithConfigs);
		aWSs3StorageConfig2.setAppKey("appKey2");
		aWSs3StorageConfig2.setRegion("region2");
		aWSs3StorageConfig2.setEncodedAppSecret("encodedAppSecret2");
		aWSs3StorageConfig2.setBucketName("bucketName2");

		DropBoxStorageConfigModel dropBoxStorageConfig1 = modelService.create(DropBoxStorageConfigModel.class);
		dropBoxStorageConfig1.setCode(DROP_BOX_STORAGE_CONFIG_1_CODE);
		dropBoxStorageConfig1.setName(DROP_BOX_STORAGE_CONFIG_1_CODE);
		dropBoxStorageConfig1.setType(dropBoxStorageType);
		dropBoxStorageConfig1.setUser(userWithConfigs);
		dropBoxStorageConfig1.setAppKey("appKey");
		dropBoxStorageConfig1.setEncodedAppSecret("appSecret");

		modelService.saveAll();
	}

	@Test(expected = UnknownIdentifierException.class)
	public void testGetStorageConfigsDataByTypeCodeTypeNotExists()
	{
		storageConfigFacade.getStorageConfigsDataByTypeCode("not-exists");
	}

	@Test
	public void testGetStorageConfigsDataByTypeCodeUserWithoutConfigs()
	{
		userService.setCurrentUser(userWithoutConfigs);
		List<StorageConfigData> aWSs3StorageConfigDataList = storageConfigFacade
				.getStorageConfigsDataByTypeCode(AWS_S3_STORAGE_TYPE_CODE);

		assertNotNull(aWSs3StorageConfigDataList);
		assertTrue(aWSs3StorageConfigDataList.isEmpty());
	}

	@Test
	public void testGetStorageConfigsDataByTypeCode()
	{
		userService.setCurrentUser(userWithConfigs);

		List<StorageConfigData> aWSs3StorageConfigDataList = storageConfigFacade
				.getStorageConfigsDataByTypeCode(AWS_S3_STORAGE_TYPE_CODE);

		assertNotNull(aWSs3StorageConfigDataList);
		assertEquals(2, aWSs3StorageConfigDataList.size());

		List<StorageConfigData> dropBoxStorageConfigDataList = storageConfigFacade
				.getStorageConfigsDataByTypeCode(DROP_BOX_STORAGE_TYPE_CODE);

		assertNotNull(dropBoxStorageConfigDataList);
		assertEquals(1, dropBoxStorageConfigDataList.size());
	}

	@Test
	public void testGetStorageConfigData()
	{
		StorageConfigData aWSs3StorageConfig1Data = storageConfigFacade.getStorageConfigData(AWS_S3_STORAGE_CONFIG1_CODE);

		assertNotNull(aWSs3StorageConfig1Data);

		StorageConfigData dropBoxStorageConfig1Data = storageConfigFacade.getStorageConfigData(DROP_BOX_STORAGE_CONFIG_1_CODE);

		assertNotNull(dropBoxStorageConfig1Data);
	}

	@Test(expected = UnknownIdentifierException.class)
	public void testGetStorageConfigDataCodeNotExists()
	{
		storageConfigFacade.getStorageConfigData("not-exists");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetStorageConfigDataNoSuchConfigTypeConverter()
	{
		StorageTypeModel storageTypeModel = modelService.create(StorageTypeModel.class);
		storageTypeModel.setCode("no-such-config-type-converter");
		storageTypeModel.setName("name");
		modelService.save(storageTypeModel);

		aWSs3StorageConfig1.setType(storageTypeModel);
		modelService.save(aWSs3StorageConfig1);

		storageConfigFacade.getStorageConfigData(AWS_S3_STORAGE_CONFIG1_CODE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetStorageConfigsDataByTypeCodeNoSuchConfigTypeConverter()
	{
		StorageTypeModel storageTypeModel = modelService.create(StorageTypeModel.class);
		String code = "no-such-config-type-converter";
		storageTypeModel.setCode(code);
		storageTypeModel.setName("name");
		modelService.save(storageTypeModel);

		storageConfigFacade.getStorageConfigsDataByTypeCode(code);
	}

	@Test
	public void testRemoveStorageConfig()
	{
		userService.setCurrentUser(userWithConfigs);

		List<StorageConfigData> storageConfigDataList = storageConfigFacade.getStorageConfigsDataByTypeCode(AWS_S3_STORAGE_TYPE_CODE);
		assertEquals(2, storageConfigDataList.size());

		storageConfigFacade.removeStorageConfig(AWS_S3_STORAGE_CONFIG1_CODE);

		storageConfigDataList = storageConfigFacade.getStorageConfigsDataByTypeCode(AWS_S3_STORAGE_TYPE_CODE);
		assertEquals(1, storageConfigDataList.size());
	}
}
