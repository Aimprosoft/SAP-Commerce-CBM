package com.aimprosoft.importexportcloud.converters;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;
import com.aimprosoft.importexportcloud.model.AWSs3StorageConfigModel;
import com.aimprosoft.importexportcloud.model.StorageTypeModel;
import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;


@IntegrationTest
public class DefaultAWSs3StorageConfigReverseConverterIntegrationTest extends ServicelayerTransactionalTest
{
	private static final String aWSs3StorageTypeCode = "aWSs3StorageType";
	private static final String aWSs3StorageTypeName = "aWSs3StorageTypeName";
	private static final String aWSs3StorageConfigCode = "aWSs3StorageConfigCode";
	private static final String aWSs3StorageConfigName = "aWSs3StorageConfigName";
	private static final String aWSs3StorageConfigAppKey = "aWSs3StorageConfigAppKey";
	private static final String aWSs3StorageConfigRegion = "aWSs3StorageConfigRegion";
	private static final String aWSs3StorageConfigEncodedAppSecret = "aWSs3StorageConfigEncodedAppSecret";

	@Resource(name = "aWSs3StorageConfigReverseConverter")
	private Converter<StorageConfigData, AWSs3StorageConfigModel> converter;

	@Resource
	private ModelService modelService;

	private StorageConfigData aWSs3StorageConfigData;
	private StorageTypeModel storageTypeModel;

	@Before
	public void setUp()
	{
		storageTypeModel = modelService.create(StorageTypeModel.class);
		storageTypeModel.setCode(aWSs3StorageTypeCode);
		storageTypeModel.setName(aWSs3StorageTypeName);
		modelService.saveAll(storageTypeModel);

		aWSs3StorageConfigData = new StorageConfigData();
		aWSs3StorageConfigData.setCode(aWSs3StorageConfigCode);
		aWSs3StorageConfigData.setName(aWSs3StorageConfigName);
		StorageTypeData aWSStorageTypeData = new StorageTypeData();
		aWSStorageTypeData.setCode(aWSs3StorageTypeCode);
		aWSs3StorageConfigData.setStorageTypeData(aWSStorageTypeData);
		aWSs3StorageConfigData.setAppKey(aWSs3StorageConfigAppKey);
		aWSs3StorageConfigData.setRegion(aWSs3StorageConfigRegion);
		aWSs3StorageConfigData.setEncodedAppSecret(aWSs3StorageConfigEncodedAppSecret);

	}

	@Test
	public void testConvert()
	{
		AWSs3StorageConfigModel storageConfigModel = converter.convert(aWSs3StorageConfigData);

		assertEquals(aWSs3StorageConfigName, storageConfigModel.getName());
		assertEquals(storageTypeModel, storageConfigModel.getType());
		assertEquals(aWSs3StorageConfigAppKey, storageConfigModel.getAppKey());
		assertEquals(aWSs3StorageConfigRegion, storageConfigModel.getRegion());
		assertEquals(aWSs3StorageConfigEncodedAppSecret, storageConfigModel.getEncodedAppSecret());
	}
}
