package com.aimprosoft.importexportcloud.providers.impl;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import de.hybris.bootstrap.annotations.UnitTest;
import org.apache.logging.log4j.util.Strings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DropboxConnectionProviderUnitTest
{
    private static final String VALID_PATH = "id:TpzJX6PfFoAAAAAAAAAdNA";
    private static final String NOT_VALID_PATH = "ipzJX6PfFoAAAAAAAAAdNA";
    private static final String STORAGE_CODE = "testStorageConfigDataCode";
    private static final String AUTH_CODE = "testAuthCode";
    private static final String DEFAULT_ENSURED_PATH = "/";

    private DefaultDropboxConnectionProvider defaultDropboxConnectionProvider;

    @Before
    public void setUp()
    {
        defaultDropboxConnectionProvider = new DefaultDropboxConnectionProvider();
    }

    @Test
    public void isDropBoxPathValid()
    {
        assertTrue(defaultDropboxConnectionProvider.isDropBoxPathValid(VALID_PATH));
        assertFalse(defaultDropboxConnectionProvider.isDropBoxPathValid(NOT_VALID_PATH));

    }

    @Test(expected = NullPointerException.class)
    public void isDropBoxPathValidWithNullPath()
    {
        defaultDropboxConnectionProvider.isDropBoxPathValid(null);

    }

    @Test
    public void resolveAuthCode() {
        String [] authCodeArray = {STORAGE_CODE, AUTH_CODE};
        String authCode = defaultDropboxConnectionProvider.resolveAuthCode(authCodeArray, STORAGE_CODE);

        assertEquals(AUTH_CODE, authCode);
    }

    @Test
    public void resolveAuthCodeWithInvalidData() {
        String [] withWrongSize = {STORAGE_CODE, AUTH_CODE, "mock"};
        String [] withWrongStorageCode = {"mock", AUTH_CODE};
        String authCode = defaultDropboxConnectionProvider.resolveAuthCode(withWrongSize, STORAGE_CODE);

        assertEquals(Strings.EMPTY, authCode);

        authCode = defaultDropboxConnectionProvider.resolveAuthCode(withWrongStorageCode, STORAGE_CODE);

        assertEquals(Strings.EMPTY, authCode);
    }

    @Test
    public void getEnsuredPath() throws CloudStorageException
    {
       String ensuredPath = defaultDropboxConnectionProvider.getEnsuredPath(VALID_PATH);
       assertEquals(DEFAULT_ENSURED_PATH + VALID_PATH, ensuredPath);

       ensuredPath = defaultDropboxConnectionProvider.getEnsuredPath(Strings.EMPTY);
       assertEquals(DEFAULT_ENSURED_PATH, ensuredPath);
       ensuredPath = defaultDropboxConnectionProvider.getEnsuredPath(null);
       assertEquals(DEFAULT_ENSURED_PATH, ensuredPath);
    }


}
