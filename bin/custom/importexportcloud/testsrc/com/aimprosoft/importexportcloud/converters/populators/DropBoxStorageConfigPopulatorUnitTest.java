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
public class DropBoxStorageConfigPopulatorUnitTest
{

	private static final String TEST_ACCESS_TOKEN = "testAccessToken";

	private final Populator<DropBoxStorageConfigModel, StorageConfigData> populator = new DropBoxStorageConfigPopulator();

	private final DropBoxStorageConfigModel source = mock(DropBoxStorageConfigModel.class);
	private final StorageConfigData target = spy(new StorageConfigData());

	@Before
	public void setUp()
	{
		initMocks(this);
	}

	@Test
	public void populateTest()
	{
		when(source.getAccessToken()).thenReturn(TEST_ACCESS_TOKEN);

		populator.populate(source, target);

		assertEquals(TEST_ACCESS_TOKEN, target.getAccessToken());
		assertEquals("/", target.getRootFolder());
	}

	@Test
	public void sourceIsNullTest()
	{
		populator.populate(null, target);
		verifyZeroInteractions(target);
	}
}
