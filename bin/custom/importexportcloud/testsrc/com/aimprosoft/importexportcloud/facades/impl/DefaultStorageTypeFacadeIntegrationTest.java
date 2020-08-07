package com.aimprosoft.importexportcloud.facades.impl;

import com.aimprosoft.importexportcloud.facades.StorageTypeFacade;
import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;
import com.aimprosoft.importexportcloud.model.StorageTypeModel;
import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;


@IntegrationTest
public class DefaultStorageTypeFacadeIntegrationTest extends ServicelayerTransactionalTest
{
	@Resource
	private StorageTypeFacade storageTypeFacade;

	@Resource
	private ModelService modelService;

	private static final String AWS_S3_STORAGE_TYPE_CODE = "aWSs3StorageType";
	private static final String DROP_BOX_STORAGE_TYPE_CODE = "dropBoxStorageType";

	@Before
	public void setUp()
	{
		StorageTypeModel aWSs3StorageType = modelService.create(StorageTypeModel.class);
		aWSs3StorageType.setCode(AWS_S3_STORAGE_TYPE_CODE);
		aWSs3StorageType.setName(AWS_S3_STORAGE_TYPE_CODE);

		StorageTypeModel dropBoxStorageType = modelService.create(StorageTypeModel.class);
		dropBoxStorageType.setCode(DROP_BOX_STORAGE_TYPE_CODE);
		dropBoxStorageType.setName(DROP_BOX_STORAGE_TYPE_CODE);

		modelService.saveAll();
	}

	@Test
	public void testGetStorageTypesData()
	{
		List<StorageTypeData> storageTypeDataList = storageTypeFacade.getStorageTypesData();
		Assert.assertEquals(2, storageTypeDataList.size());
	}
}
