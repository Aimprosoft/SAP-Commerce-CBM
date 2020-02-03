package com.aimprosoft.importexportcloud.converters;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.model.AWSs3StorageConfigModel;
import com.aimprosoft.importexportcloud.model.StorageTypeModel;
import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;


@IntegrationTest
public class DefaultAWSs3StorageConfigConverterIntegrationTest extends ServicelayerTransactionalTest
{
	private static final String aWSs3StorageTypeCode = "aWSs3StorageType";
	private static final String aWSs3StorageTypeName = "aWSs3StorageTypeName";
	private static final String aWSs3StorageConfigCode = "aWSs3StorageConfigCode";
	private static final String aWSs3StorageConfigName = "aWSs3StorageConfigName";
	private static final String aWSs3StorageConfigAppKey = "aWSs3StorageConfigAppKey";
	private static final String aWSs3StorageConfigRegion = "aWSs3StorageConfigRegion";
	private static final String aWSs3StorageConfigEncodedAppSecret = "aWSs3StorageConfigEncodedAppSecret";
	private static final String aWSs3StorageConfigBucketName = "aWSs3StorageConfigBucketName";

	@Resource(name = "aWSs3StorageConfigConverter")
	private Converter<AWSs3StorageConfigModel, StorageConfigData> converter;

	@Resource
	private ModelService modelService;

	@Resource
	private UserService userService;

	private AWSs3StorageConfigModel aWSs3StorageConfigModel;
	private StorageTypeModel storageTypeModel;

	@Before
	public void setUp()
	{
		storageTypeModel = modelService.create(StorageTypeModel.class);
		storageTypeModel.setCode(aWSs3StorageTypeCode);
		storageTypeModel.setName(aWSs3StorageTypeName);

		aWSs3StorageConfigModel = modelService.create(AWSs3StorageConfigModel.class);
		aWSs3StorageConfigModel.setUser(userService.getCurrentUser());
		aWSs3StorageConfigModel.setCode(aWSs3StorageConfigCode);
		aWSs3StorageConfigModel.setName(aWSs3StorageConfigName);
		aWSs3StorageConfigModel.setType(storageTypeModel);
		aWSs3StorageConfigModel.setAppKey(aWSs3StorageConfigAppKey);
		aWSs3StorageConfigModel.setRegion(aWSs3StorageConfigRegion);
		aWSs3StorageConfigModel.setEncodedAppSecret(aWSs3StorageConfigEncodedAppSecret);
		aWSs3StorageConfigModel.setBucketName(aWSs3StorageConfigBucketName);

		modelService.saveAll();
	}

	@Test
	public void testConvert()
	{
		StorageConfigData storageConfigData = converter.convert(aWSs3StorageConfigModel);

		assertEquals(aWSs3StorageConfigCode, storageConfigData.getCode());
		assertEquals(aWSs3StorageConfigName, storageConfigData.getName());
		assertEquals(storageTypeModel.getCode(), storageConfigData.getStorageTypeData().getCode());
		assertEquals(aWSs3StorageConfigAppKey, storageConfigData.getAppKey());
		assertEquals(aWSs3StorageConfigRegion, storageConfigData.getRegion());
		assertEquals(aWSs3StorageConfigEncodedAppSecret, storageConfigData.getEncodedAppSecret());
		assertEquals(aWSs3StorageConfigBucketName, storageConfigData.getBucketName());
	}
}
