package com.aimprosoft.importexportcloud.service.impl;

import com.aimprosoft.importexportcloud.model.StorageConfigModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

@UnitTest
public class DefaultStorageConfigServiceUnitTest
{
    private DefaultStorageConfigService storageConfigService;

    @Before
    public void setUp()
    {
        storageConfigService = new DefaultStorageConfigService();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAllStorageConfigsByUserAndTypeUserIsNull()
    {
        storageConfigService.getAllStorageConfigsByUserAndTypeCode(null, "string");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAllStorageConfigsByUserAndTypeTypeIsNull()
    {
        storageConfigService.getAllStorageConfigsByUserAndTypeCode(new UserModel(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStorageConfigByCodeCodeIsNull()
    {
        storageConfigService.getStorageConfigByCode(null);
    }

    @Test(expected = UnknownIdentifierException.class)
    public void testGetStorageConfigByCodeEmptyResult()
    {
        GenericDao mockDao = mock(GenericDao.class);
        storageConfigService.setStorageConfigDao(mockDao);

        when(mockDao.find(anyMap())).thenReturn(Collections.EMPTY_LIST);
        storageConfigService.getStorageConfigByCode(anyString());
    }

    @Test(expected = AmbiguousIdentifierException.class)
    public void testGetStorageConfigByCodeNotSingleResult()
    {
        GenericDao mockDao = mock(GenericDao.class);
        storageConfigService.setStorageConfigDao(mockDao);

        when(mockDao.find(anyMap())).thenReturn(Arrays.asList(new Object(), new Object()));
        storageConfigService.getStorageConfigByCode(anyString());
    }

    @Test
    public void testGetStorageConfigByCodeSingleResult()
    {
        GenericDao mockDao = mock(GenericDao.class);
        storageConfigService.setStorageConfigDao(mockDao);

        StorageConfigModel storageConfigModelExpected = new StorageConfigModel();
        when(mockDao.find(anyMap())).thenReturn(Arrays.asList(storageConfigModelExpected));
        StorageConfigModel storageConfigByCodeActual = storageConfigService.getStorageConfigByCode(anyString());

        assertSame(storageConfigModelExpected, storageConfigByCodeActual);
    }
}
