package com.aimprosoft.importexportcloud.converters.populators;

import com.aimprosoft.importexportcloud.facades.data.CloudObjectData;
import com.dropbox.core.v2.files.Metadata;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.converters.Populator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


@UnitTest
public class FileMetadataCloudObjectDataPopulatorUnitTest
{

	private static final String TEST_PATH_DISPLAY = "testPathDisplay";

	private final Populator<Metadata, CloudObjectData> populator = new FileMetadataCloudObjectDataPopulator();

	private final Metadata source = mock(Metadata.class);
	private final CloudObjectData target = spy(new CloudObjectData());

	@Before
	public void setUp()
	{
		initMocks(this);
	}

	@Test
	public void populateTest()
	{
		when(source.getPathDisplay()).thenReturn(TEST_PATH_DISPLAY + "/");

		populator.populate(source, target);

		assertEquals(TEST_PATH_DISPLAY, target.getTitle());
		assertEquals(TEST_PATH_DISPLAY + "/", target.getPathDisplay());
	}

	@Test
	public void sourceIsNullTest()
	{
		populator.populate(null, target);
		verifyZeroInteractions(target);
	}
}
