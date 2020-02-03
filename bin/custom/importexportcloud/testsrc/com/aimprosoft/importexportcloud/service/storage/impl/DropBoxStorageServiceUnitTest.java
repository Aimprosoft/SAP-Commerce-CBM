package com.aimprosoft.importexportcloud.service.storage.impl;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;
import com.aimprosoft.importexportcloud.providers.DropboxConnectionProvider;
import com.aimprosoft.importexportcloud.service.StorageConfigService;
import com.aimprosoft.importexportcloud.service.connection.impl.DefaultDropBoxConnectionService;
import com.aimprosoft.importexportcloud.service.validators.StorageConfigValidator;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxWebAuth;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.util.Config;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Config.class })
public class DropBoxStorageServiceUnitTest
{
	private static final String TEST_CONFIG_CODE = "testCode";

	private static final String DROPBOX_AUTH_CODE_ATTRIBUTE = "dropBoxAuthCode";

	private static final String DROPBOX_AUTH_CODE = "simpleValid";

	private static final String DROPBOX_APP = "testApp";

	private static final String DROPBOX_ENCODED_SECRET = "secretEncoded";

	private DefaultDropBoxConnectionService dropBoxConnectionService;

	@Mock
	private SessionService sessionService;

	@Mock
	private StorageConfigValidator storageConfigValidator;

	@Mock
	private DropboxConnectionProvider connectionProvider;

	@Mock
	private StorageConfigService storageConfigService;

	@Before
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);
		dropBoxConnectionService = Mockito.spy(new DefaultDropBoxConnectionService());
		dropBoxConnectionService.setSessionService(sessionService);
		dropBoxConnectionService.setStorageConfigValidator(storageConfigValidator);
		dropBoxConnectionService.setStorageConfigService(storageConfigService);
		dropBoxConnectionService.setClientIdentifier("testId");
		dropBoxConnectionService.setDropboxConnectionProvider(connectionProvider);
	}

	@Test
	public void testConnection() throws CloudStorageException, DbxException
	{
		StorageConfigData config = getStorageConfig();

		PowerMockito.mockStatic(Config.class);

		String key = "cloud.storage." + config.getStorageTypeData().getCode() + ".redirectUrl";
		when(Config.getString(key, "")).thenReturn("testUrl");

		DbxWebAuth dbxWebAuth = mock(DbxWebAuth.class);

		DbxAuthFinish dbxAuthFinish = new DbxAuthFinish("validToken", "", "", "", "");

		when(dbxWebAuth.finishFromCode("simpleValid", "testUrl")).thenReturn(dbxAuthFinish);

		doNothing().when(storageConfigService).setDropBoxConnectionStatus(config.getCode(), dbxAuthFinish.getAccessToken(), Boolean.FALSE);

		doNothing().when(storageConfigValidator).validate(config);

		when(sessionService.getAttribute(DROPBOX_AUTH_CODE_ATTRIBUTE))
				.thenReturn(new String[] { "test", DROPBOX_AUTH_CODE });

		when(connectionProvider.resolveAuthCode(new String[] { "test", DROPBOX_AUTH_CODE }, TEST_CONFIG_CODE)).thenReturn(DROPBOX_AUTH_CODE);

		when(connectionProvider.getAccessToken(config, "testId")).thenReturn("testToken");

		dropBoxConnectionService.connect(config);

		assertTrue(config.getAuthCode().equalsIgnoreCase(DROPBOX_AUTH_CODE));
	}

	private StorageConfigData getStorageConfig()
	{
		StorageConfigData storageConfigData = new StorageConfigData();
		storageConfigData.setCode(TEST_CONFIG_CODE);
		StorageTypeData storageTypeData = new StorageTypeData();
		storageTypeData.setCode("testType");
		storageConfigData.setStorageTypeData(storageTypeData);
		storageConfigData.setAppKey(DROPBOX_APP);
		storageConfigData.setEncodedAppSecret(DROPBOX_ENCODED_SECRET);
		return storageConfigData;
	}
}
