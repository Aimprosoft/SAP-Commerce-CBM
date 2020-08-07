package com.aimprosoft.importexportcloud.service.impl;

import com.aimprosoft.importexportcloud.model.StorageTypeModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
public class DefaultStorageTypeServiceUnitTest
{
    private DefaultStorageTypeService storageTypeService;

    @Before
    public void setUp()
    {
        storageTypeService = new DefaultStorageTypeService();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStorageTypeByCodeCodeIsNull()
    {
        storageTypeService.getStorageTypeByCode(null);
    }

    @Test(expected = UnknownIdentifierException.class)
    public void testGetStorageTypeByCodeEmptyResult()
    {
        GenericDao mockDao = mock(GenericDao.class);
        storageTypeService.setStorageTypeDao(mockDao);

        when(mockDao.find(anyMap())).thenReturn(Collections.EMPTY_LIST);
        storageTypeService.getStorageTypeByCode(anyString());
    }

    @Test(expected = AmbiguousIdentifierException.class)
    public void testGetStorageTypeByCodeNotSingleResult()
    {
        GenericDao mockDao = mock(GenericDao.class);
        storageTypeService.setStorageTypeDao(mockDao);

        when(mockDao.find(anyMap())).thenReturn(Arrays.asList(new Object(), new Object()));
        storageTypeService.getStorageTypeByCode(anyString());
    }

    @Test
    public void testGetStorageTypeByCodeSingleResult()
    {
        GenericDao mockDao = mock(GenericDao.class);
        storageTypeService.setStorageTypeDao(mockDao);

        StorageTypeModel storageTypeModelExpected = new StorageTypeModel();
        when(mockDao.find(anyMap())).thenReturn(Arrays.asList(storageTypeModelExpected));
        StorageTypeModel storageTypeModelActual = storageTypeService.getStorageTypeByCode(anyString());

        assertSame(storageTypeModelExpected, storageTypeModelActual);
    }
}
