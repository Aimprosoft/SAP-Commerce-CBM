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
public class AWSs3StorageConfigReversePopulatorUnitTest
{

	private static final String TEST_APP_KEY = "testAppKey";
	private static final String TEST_REGION = "testRegion";
	private static final String TEST_ENCODED_APP_SECRET = "encodedAppSecret";
	private static final String TEST_BUCKET_NAME = "bucketName";

	private final Populator<StorageConfigData, AWSs3StorageConfigModel> populator = new AWSs3StorageConfigReversePopulator();

	private final StorageConfigData source = mock(StorageConfigData.class);
	private final AWSs3StorageConfigModel target = spy(new AWSs3StorageConfigModel());

	@Before
	public void setUp()
	{
		initMocks(this);
	}

	@Test
	public void populateTest()
	{
		when(source.getAppKey()).thenReturn(TEST_APP_KEY);
		when(source.getRegion()).thenReturn(TEST_REGION);
		when(source.getEncodedAppSecret()).thenReturn(TEST_ENCODED_APP_SECRET);
		when(source.getBucketName()).thenReturn(TEST_BUCKET_NAME);

		populator.populate(source, target);

		assertEquals(TEST_APP_KEY, target.getAppKey());
		assertEquals(TEST_REGION, target.getRegion());
		assertEquals(TEST_ENCODED_APP_SECRET, target.getEncodedAppSecret());
		assertEquals(TEST_BUCKET_NAME, target.getBucketName());
	}

	@Test
	public void sourceIsNullTest()
	{
		populator.populate(null, target);
		verifyZeroInteractions(target);
	}
}
