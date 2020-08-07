package com.aimprosoft.importexportcloud.converters.populators;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.model.DropBoxStorageConfigModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.converters.Populator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


@UnitTest
public class DropBoxStorageConfigReversePopulatorUnitTest
{

	private static final String TEST_APP_KEY = "testAppKey";
	private static final String TEST_ACCESS_TOKEN = "testAccessToken";
	private static final String TEST_ENCODED_APP_SECRET = "testEncodedAppSecret";

	private final Populator<StorageConfigData, DropBoxStorageConfigModel> populator = new DropBoxStorageConfigReversePopulator();

	private final StorageConfigData source = mock(StorageConfigData.class);
	private final DropBoxStorageConfigModel target = spy(new DropBoxStorageConfigModel());

	@Before
	public void setUp()
	{
		initMocks(this);
	}

	@Test
	public void populateTest()
	{
		when(source.getAppKey()).thenReturn(TEST_APP_KEY);
		when(source.getAccessToken()).thenReturn(TEST_ACCESS_TOKEN);
		when(source.getEncodedAppSecret()).thenReturn(TEST_ENCODED_APP_SECRET);

		populator.populate(source, target);

		assertEquals(TEST_APP_KEY, target.getAppKey());
		assertEquals(TEST_ENCODED_APP_SECRET, target.getEncodedAppSecret());
		assertEquals(TEST_ACCESS_TOKEN, target.getAccessToken());
	}

	@Test
	public void sourceIsNullTest()
	{
		populator.populate(null, target);
		verifyZeroInteractions(target);
	}
}
