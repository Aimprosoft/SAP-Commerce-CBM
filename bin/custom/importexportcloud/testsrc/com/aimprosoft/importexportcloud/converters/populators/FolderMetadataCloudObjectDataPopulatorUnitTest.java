package com.aimprosoft.importexportcloud.converters.populators;

import com.aimprosoft.importexportcloud.facades.data.CloudObjectData;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.Metadata;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.converters.Populator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.MockitoAnnotations.initMocks;


@UnitTest
public class FolderMetadataCloudObjectDataPopulatorUnitTest
{
	private static final String TEST_ID = "testId";

	private final Populator<Metadata, CloudObjectData> populator = new FolderMetadataCloudObjectDataPopulator();

	private final Metadata source = new FolderMetadata("testName", TEST_ID, "pathLower", "pathToDisplay",
			null, null, null, null);
	private final CloudObjectData target = spy(new CloudObjectData());

	@Before
	public void setUp()
	{
		initMocks(this);
	}

	@Test
	public void populateTest()
	{
		populator.populate(source, target);

		assertEquals(TEST_ID, target.getName());
		assertTrue(target.isFolder());
	}

	@Test
	public void sourceIsNullTest()
	{
		populator.populate(null, target);
		verifyZeroInteractions(target);
	}
}
