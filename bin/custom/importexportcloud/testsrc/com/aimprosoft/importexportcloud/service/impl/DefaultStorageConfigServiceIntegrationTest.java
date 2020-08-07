package com.aimprosoft.importexportcloud.service.impl;

import com.aimprosoft.importexportcloud.model.AWSs3StorageConfigModel;
import com.aimprosoft.importexportcloud.model.DropBoxStorageConfigModel;
import com.aimprosoft.importexportcloud.model.StorageConfigModel;
import com.aimprosoft.importexportcloud.model.StorageTypeModel;
import com.aimprosoft.importexportcloud.service.StorageConfigService;
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
public class DefaultStorageConfigServiceIntegrationTest extends ServicelayerTransactionalTest
{
    @Resource
    private ModelService modelService;

    @Resource
    private StorageConfigService storageConfigService;

    @Resource
    private UserService userService;

    private StorageTypeModel aWSs3StorageType;

    private StorageTypeModel dropBoxStorageType;

    private UserModel userWithConfigs;

    private UserModel userWithOutConfigs;

    @Before
    public void setUp() throws Exception
    {
        createCoreData();
        createDefaultUsers();
        aWSs3StorageType = modelService.create(StorageTypeModel.class);
        aWSs3StorageType.setCode("AWSs3");
        aWSs3StorageType.setName("AWSs3");

        dropBoxStorageType = modelService.create(StorageTypeModel.class);
        dropBoxStorageType.setCode("dropBox");
        dropBoxStorageType.setName("dropBox");

        modelService.saveAll();

        userWithConfigs = userService.getUserForUID("ariel");
        userWithOutConfigs = userService.getUserForUID("demo");
    }

    @Test
    public void testUserHasNoConfigs()
    {
        List<StorageConfigModel> aWSs3StorageConfigList =
                storageConfigService.getAllStorageConfigsByUserAndTypeCode(userWithOutConfigs, aWSs3StorageType.getCode());
        assertNotNull(aWSs3StorageConfigList);
        assertTrue(aWSs3StorageConfigList.isEmpty());
    }

    @Test
    public void testGetAllStorageConfigsByUserAndType()
    {
        AWSs3StorageConfigModel aWSs3StorageConfig1 = modelService.create(AWSs3StorageConfigModel.class);
        aWSs3StorageConfig1.setCode("aWSs3StorageConfig1");
        aWSs3StorageConfig1.setName("AWS s3 Storage Config 1");
        aWSs3StorageConfig1.setType(aWSs3StorageType);
        aWSs3StorageConfig1.setUser(userWithConfigs);
        aWSs3StorageConfig1.setAppKey("testAppKey1");
        aWSs3StorageConfig1.setEncodedAppSecret("testEncodedAppSecret1");
        aWSs3StorageConfig1.setRegion("test-region-1");
        aWSs3StorageConfig1.setBucketName("test-bucket-name-1");

        AWSs3StorageConfigModel aWSs3StorageConfig2 = modelService.create(AWSs3StorageConfigModel.class);
        aWSs3StorageConfig2.setCode("aWSs3StorageConfig2");
        aWSs3StorageConfig2.setName("AWS s3 Storage Config 2");
        aWSs3StorageConfig2.setType(aWSs3StorageType);
        aWSs3StorageConfig2.setUser(userWithConfigs);
        aWSs3StorageConfig2.setAppKey("testAppKey2");
        aWSs3StorageConfig2.setEncodedAppSecret("testEncodedAppSecret2");
        aWSs3StorageConfig2.setRegion("test-region-2");
        aWSs3StorageConfig2.setBucketName("test-bucket-name-2");


        DropBoxStorageConfigModel dropBoxStorageConfig1 = modelService.create(DropBoxStorageConfigModel.class);
        dropBoxStorageConfig1.setCode("dropBoxStorageConfig1");
        dropBoxStorageConfig1.setName("Dropbox Storage Config 1");
        dropBoxStorageConfig1.setType(dropBoxStorageType);
        dropBoxStorageConfig1.setUser(userWithConfigs);
        dropBoxStorageConfig1.setAppKey("testAppKeyDb");
        dropBoxStorageConfig1.setEncodedAppSecret("testAppSecretKeyDb");

        modelService.saveAll();

        List<StorageConfigModel> aWSs3StorageConfigList = storageConfigService.getAllStorageConfigsByUserAndTypeCode(userWithConfigs, aWSs3StorageType.getCode());
        assertEquals(2, aWSs3StorageConfigList.size());
        assertTrue(aWSs3StorageConfigList.contains(aWSs3StorageConfig1));

        List<StorageConfigModel> dropBoxStorageConfigList = storageConfigService.getAllStorageConfigsByUserAndTypeCode(userWithConfigs, dropBoxStorageType.getCode());
        assertEquals(1, dropBoxStorageConfigList.size());
        assertTrue(dropBoxStorageConfigList.contains(dropBoxStorageConfig1));
    }

    @Test(expected = UnknownIdentifierException.class)
    public void testGetStorageConfigByCodeNotFound()
    {
        String storageConfigCode = "aWSs3StorageConfig";
        storageConfigService.getStorageConfigByCode(storageConfigCode);
    }

    @Test
    public void testGetStorageConfigByCode()
    {
        String storageConfigCode = "aWSs3StorageConfig";
        String storageConfigName = "AWS s3 Storage Config";
        String storageConfigAppKey = "testAppKey";
        String storageConfigAppSecret = "testEncodedAppSecret";
        String region = "test-region-1";
        String bucketName = "test-bucket-name-1";

        AWSs3StorageConfigModel aWSs3StorageConfig = modelService.create(AWSs3StorageConfigModel.class);
        aWSs3StorageConfig.setCode(storageConfigCode);
        aWSs3StorageConfig.setName(storageConfigName);
        aWSs3StorageConfig.setType(aWSs3StorageType);
        aWSs3StorageConfig.setUser(userWithConfigs);
        aWSs3StorageConfig.setAppKey(storageConfigAppKey);
        aWSs3StorageConfig.setEncodedAppSecret(storageConfigAppSecret);
        aWSs3StorageConfig.setRegion(region);
        aWSs3StorageConfig.setBucketName(bucketName);

        modelService.save(aWSs3StorageConfig);

        StorageConfigModel storageConfigByCode = storageConfigService.getStorageConfigByCode(storageConfigCode);

        assertNotNull(storageConfigByCode);
    }
}
