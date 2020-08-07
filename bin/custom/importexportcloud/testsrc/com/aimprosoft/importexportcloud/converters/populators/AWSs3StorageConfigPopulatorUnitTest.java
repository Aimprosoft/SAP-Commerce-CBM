package com.aimprosoft.importexportcloud.converters.populators;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.model.AWSs3StorageConfigModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.converters.Populator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


@UnitTest
public class AWSs3StorageConfigPopulatorUnitTest
{

	private static final String TEST_REGION = "testRegion";
	private static final String TEST_BUCKET_NAME = "testBucketName";
	private static final String ROOT_FOLDER = "/";

	private final Populator<AWSs3StorageConfigModel, StorageConfigData> populator = new AWSs3StorageConfigPopulator();

	private final AWSs3StorageConfigModel source = mock(AWSs3StorageConfigModel.class);
	private final StorageConfigData target = spy(new StorageConfigData());

	@Before
	public void setUp()
	{
		initMocks(this);
	}

	@Test
	public void populateTest()
	{
		when(source.getRegion()).thenReturn(TEST_REGION);
		when(source.getBucketName()).thenReturn(TEST_BUCKET_NAME);

		populator.populate(source, target);

		assertEquals(TEST_REGION, target.getRegion());
		assertEquals(TEST_BUCKET_NAME, target.getBucketName());
		assertEquals(ROOT_FOLDER, target.getRootFolder());
	}

	@Test
	public void sourceIsNullTest()
	{
		populator.populate(null, target);
		verifyZeroInteractions(target);
	}
}
