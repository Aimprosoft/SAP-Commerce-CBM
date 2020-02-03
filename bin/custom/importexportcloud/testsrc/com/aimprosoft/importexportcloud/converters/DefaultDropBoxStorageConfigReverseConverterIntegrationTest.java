package com.aimprosoft.importexportcloud.converters;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;
import com.aimprosoft.importexportcloud.model.DropBoxStorageConfigModel;
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
public class DefaultDropBoxStorageConfigReverseConverterIntegrationTest extends ServicelayerTransactionalTest
{
	private static final String dropBoxStorageTypeCode = "dropBoxStorageType";
	private static final String dropBoxStorageTypeName = "dropBoxStorageTypeName";
	private static final String dropBoxStorageConfigCode = "dropBoxStorageConfigCode";
	private static final String dropBoxStorageConfigName = "dropBoxStorageConfigName";
	private static final String dropBoxStorageConfigAppKey = "dropBoxStorageConfigAppKey";
	private static final String dropBoxStorageConfigAppSecret = "dropBoxStorageConfigAppSecret";

	@Resource(name = "dropBoxStorageConfigReverseConverter")
	private Converter<StorageConfigData, DropBoxStorageConfigModel> converter;

	@Resource
	private ModelService modelService;

	private StorageConfigData dropBoxStorageConfigData;
	private StorageTypeModel storageTypeModel;

	@Before
	public void setUp()
	{
		storageTypeModel = modelService.create(StorageTypeModel.class);
		storageTypeModel.setCode(dropBoxStorageTypeCode);
		storageTypeModel.setName(dropBoxStorageTypeName);

		dropBoxStorageConfigData = new StorageConfigData();
		dropBoxStorageConfigData.setCode(dropBoxStorageConfigCode);
		dropBoxStorageConfigData.setName(dropBoxStorageConfigName);
		StorageTypeData dropBoxStorageTypeData = new StorageTypeData();
		dropBoxStorageTypeData.setCode(dropBoxStorageTypeCode);
		dropBoxStorageConfigData.setStorageTypeData(dropBoxStorageTypeData);
		dropBoxStorageConfigData.setAppKey(dropBoxStorageConfigAppKey);
		dropBoxStorageConfigData.setEncodedAppSecret(dropBoxStorageConfigAppSecret);

		modelService.saveAll();
	}

	@Test
	public void testConvert()
	{
		DropBoxStorageConfigModel storageConfigModel = converter.convert(dropBoxStorageConfigData);

		assertEquals(dropBoxStorageConfigName, storageConfigModel.getName());
		assertEquals(storageTypeModel, storageConfigModel.getType());
		assertEquals(dropBoxStorageConfigAppKey, storageConfigModel.getAppKey());
		assertEquals(dropBoxStorageConfigAppSecret, storageConfigModel.getEncodedAppSecret());
	}
}
