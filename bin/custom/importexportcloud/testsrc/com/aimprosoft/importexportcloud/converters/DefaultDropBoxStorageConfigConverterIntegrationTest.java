package com.aimprosoft.importexportcloud.converters;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.model.DropBoxStorageConfigModel;
import com.aimprosoft.importexportcloud.model.StorageConfigModel;
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
public class DefaultDropBoxStorageConfigConverterIntegrationTest extends ServicelayerTransactionalTest
{
	private static final String dropBoxStorageTypeCode = "dropBoxStorageType";
	private static final String dropBoxStorageTypeName = "dropBoxStorageTypeName";
	private static final String dropBoxStorageConfigCode = "dropBoxStorageConfigCode";
	private static final String dropBoxStorageConfigName = "dropBoxStorageConfigName";
	private static final String dropBoxStorageConfigAppKey = "dropBoxStorageConfigAppKey";
	private static final String dropBoxStorageConfigAppSecret = "dropBoxStorageConfigAppSecret";

	@Resource(name = "dropBoxStorageConfigConverter")
	private Converter<StorageConfigModel, StorageConfigData> converter;

	@Resource
	private ModelService modelService;

	@Resource
	private UserService userService;

	private DropBoxStorageConfigModel dropBoxStorageConfigModel;
	private StorageTypeModel storageTypeModel;

	@Before
	public void setUp()
	{
		storageTypeModel = modelService.create(StorageTypeModel.class);
		storageTypeModel.setCode(dropBoxStorageTypeCode);
		storageTypeModel.setName(dropBoxStorageTypeName);

		dropBoxStorageConfigModel = modelService.create(DropBoxStorageConfigModel.class);
		dropBoxStorageConfigModel.setUser(userService.getCurrentUser());
		dropBoxStorageConfigModel.setCode(dropBoxStorageConfigCode);
		dropBoxStorageConfigModel.setName(dropBoxStorageConfigName);
		dropBoxStorageConfigModel.setType(storageTypeModel);
		dropBoxStorageConfigModel.setAppKey(dropBoxStorageConfigAppKey);
		dropBoxStorageConfigModel.setEncodedAppSecret(dropBoxStorageConfigAppSecret);

		modelService.saveAll();
	}

	@Test
	public void testConvert()
	{
		final StorageConfigData storageConfigData = converter.convert(dropBoxStorageConfigModel);

		assertEquals(dropBoxStorageConfigCode, storageConfigData.getCode());
		assertEquals(dropBoxStorageConfigName, storageConfigData.getName());
		assertEquals(storageTypeModel.getCode(), storageConfigData.getStorageTypeData().getCode());
		assertEquals(dropBoxStorageConfigAppKey, storageConfigData.getAppKey());
		assertEquals(dropBoxStorageConfigAppSecret, storageConfigData.getEncodedAppSecret());
	}
}
