package com.aimprosoft.importexportcloud.service.impl;

import com.aimprosoft.importexportcloud.model.StorageTypeModel;
import com.aimprosoft.importexportcloud.service.StorageTypeService;
import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.*;

@IntegrationTest
public class DefaultStorageTypeServiceIntegrationTest extends ServicelayerTransactionalTest
{
    @Resource
    private ModelService modelService;

    @Resource
    private StorageTypeService storageTypeService;

    private StorageTypeModel aWSs3StorageType;
    private StorageTypeModel dropBoxStorageType;
    private final String awSs3StorageTypeCode = "AWSs3StorageType";
    private final String dropBoxStorageTypeCode = "DropBoxStorageType";

    @Before
    public void setUp()
    {
        aWSs3StorageType = modelService.create(StorageTypeModel.class);
        aWSs3StorageType.setCode(awSs3StorageTypeCode);
        aWSs3StorageType.setName(awSs3StorageTypeCode);

        dropBoxStorageType = modelService.create(StorageTypeModel.class);
        dropBoxStorageType.setCode(dropBoxStorageTypeCode);
        dropBoxStorageType.setName(dropBoxStorageTypeCode);

        modelService.saveAll();
    }

    @Test(expected = UnknownIdentifierException.class)
    public void testGetStorageTypeByCodeNotExists()
    {
        storageTypeService.getStorageTypeByCode("code-not-exists");
    }

    @Test
    public void testGetStorageTypeByCode()
    {
        StorageTypeModel awSs3StorageType = storageTypeService.getStorageTypeByCode(awSs3StorageTypeCode);
        assertNotNull(awSs3StorageType);
        assertEquals(awSs3StorageTypeCode, awSs3StorageType.getCode());
    }

    @Test
    public void testGetAllStorageTypes()
    {
        List<StorageTypeModel> storageTypeList = storageTypeService.getAllStorageTypes();
        assertEquals(2, storageTypeList.size());
        assertTrue(storageTypeList.contains(aWSs3StorageType));
    }

    @Test
    public void testGetAllStorageTypesEmptyResult()
    {
        modelService.remove(aWSs3StorageType);
        modelService.remove(dropBoxStorageType);
        List<StorageTypeModel> storageTypeList = storageTypeService.getAllStorageTypes();
        assertNotNull(storageTypeList);
        assertTrue(storageTypeList.isEmpty());
    }
}
