package com.aimprosoft.importexportcloud.providers.impl;

import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.dropbox.core.DbxException;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import de.hybris.platform.servicelayer.session.SessionService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpSession;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import static org.junit.Assert.*;

public class DropboxConnectionProviderIntegrationTest extends ServicelayerTransactionalTest
{

    private static final String DROPBOX_AUTH_CODE_ATTRIBUTE = "dropBoxAuthCode";
    private static final String DROPBOX_AUTH_CODE = "simpleValid";
    private static final String CLIENT_IDENTIFIER = "AimprosoftHybrisPlugin/0.1.0";
    private static final String DROPBOX_TEST_STORAGE_CONFIG_CODE = "testDropBoxStorageConfig";
    private static final String DROPBOX_AUTH2_SUBSTRING = "https://www.dropbox.com/oauth2/authorize?response_type=code&redirect_uri=";

    private StorageConfigData storageConfigData;

    @Resource
    private DefaultDropboxConnectionProvider defaultDropboxConnectionProvider;

    @Resource
    private SessionService sessionService;

    @Resource
    private StorageConfigFacade storageConfigFacade;


    @Before
    public void setUp() throws Exception
    {
        createCoreData();
        importCsv("/test/test-user.impex", "UTF-8");
        importCsv("/test/test-storage-configs.impex", "UTF-8");
        importCsv("/test/test-warehouse-vendor-items.impex", "UTF-8");
        importCsv("/test/test-cms-catalog-site.impex", "UTF-8");
    }

    @Test
    public void getDropBoxClient()
    {
        initDTO();

        assertNotNull(defaultDropboxConnectionProvider.getDropBoxClient(CLIENT_IDENTIFIER, storageConfigData.getAccessToken()));
    }

    @Test(expected = NullPointerException.class)
    public void getDropBoxClientWithoutAccessToken()
    {
        initDTO();

        assertNotNull(defaultDropboxConnectionProvider.getDropBoxClient(CLIENT_IDENTIFIER, null));
    }

    @Test
    public void getDropBoxClientWithMockAccessToken() {
        initDTO();

        assertNotNull(defaultDropboxConnectionProvider.getDropBoxClient(CLIENT_IDENTIFIER, "mock"));
    }

    @Test
    public void getAuthorizeUrl()
    {
        initDTO();
        final HttpSession session = new MockHttpSession();
        String authUrl = defaultDropboxConnectionProvider.getAuthorizeUrl(storageConfigData, session, CLIENT_IDENTIFIER);
        assertTrue(authUrl.contains(DROPBOX_AUTH2_SUBSTRING));
    }


    private void initDTO()
    {
        storageConfigData = storageConfigFacade.getStorageConfigData(DROPBOX_TEST_STORAGE_CONFIG_CODE);
        sessionService.setAttribute(DROPBOX_AUTH_CODE_ATTRIBUTE, DROPBOX_AUTH_CODE);
    }

}
