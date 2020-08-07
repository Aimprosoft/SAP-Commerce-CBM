package com.aimprosoft.importexportcloud.converters.populators;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.model.AppKeyBasedStorageConfigModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.converters.Populator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


@UnitTest
public class AppKeyBasedStorageConfigPopulatorUnitTest
{

	private static final String TEST_APP_KEY = "testAppKey";
	private static final String TEST_ENCODED_SECRET = "testEncodedSecret";

	private final Populator<AppKeyBasedStorageConfigModel, StorageConfigData> populator = new AppKeyBasedStorageConfigPopulator();

	private final AppKeyBasedStorageConfigModel source = mock(AppKeyBasedStorageConfigModel.class);
	private final StorageConfigData target = spy(new StorageConfigData());

	@Before
	public void setUp()
	{
		initMocks(this);
	}

	@Test
	public void populateTest()
	{
		when(source.getAppKey()).thenReturn(TEST_APP_KEY);
		when(source.getEncodedAppSecret()).thenReturn(TEST_ENCODED_SECRET);
		when(source.getEnableSavingUrls()).thenReturn(true);

		populator.populate(source, target);

		assertEquals(TEST_APP_KEY, target.getAppKey());
		assertEquals(TEST_ENCODED_SECRET, target.getEncodedAppSecret());
		assertTrue(target.getEnableSavingUrls());
	}

	@Test
	public void sourceIsNullTest()
	{
		populator.populate(null, target);
		verifyZeroInteractions(target);
	}
}
