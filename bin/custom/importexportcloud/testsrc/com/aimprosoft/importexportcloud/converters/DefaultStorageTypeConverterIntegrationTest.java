package com.aimprosoft.importexportcloud.converters;

import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;
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
public class DefaultStorageTypeConverterIntegrationTest extends ServicelayerTransactionalTest
{
	private final String storageTypeCode = "storageTypeCode";
	private final String storageTypeName = "storageTypeName";
	private final Boolean storageTypeIsLocal = Boolean.TRUE;
	private final Boolean storageTypeIsDefault = Boolean.TRUE;
	@Resource(name = "storageTypeConverter")
	private Converter<StorageTypeModel, StorageTypeData> converter;

	@Resource
	private ModelService modelService;

	private StorageTypeModel storageTypeModel;

	@Before
	public void setUp()
	{
		storageTypeModel = modelService.create(StorageTypeModel.class);
		storageTypeModel.setCode(storageTypeCode);
		storageTypeModel.setName(storageTypeName);
		storageTypeModel.setLocal(storageTypeIsLocal);
		storageTypeModel.setDefault(storageTypeIsDefault);
		modelService.save(storageTypeModel);
	}

	@Test
	public void testConvert()
	{
		final StorageTypeData storageTypeData = converter.convert(storageTypeModel);
		assertEquals(storageTypeCode, storageTypeData.getCode());
		assertEquals(storageTypeName, storageTypeData.getName());
		assertEquals(storageTypeIsLocal, storageTypeData.getIsLocal());
		assertEquals(storageTypeIsDefault, storageTypeData.getIsDefault());
	}
}
