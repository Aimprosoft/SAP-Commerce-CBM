package com.aimprosoft.importexportcloud.converters.populators;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;
import com.aimprosoft.importexportcloud.model.StorageConfigModel;
import com.aimprosoft.importexportcloud.model.StorageTypeModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


@UnitTest
public class StorageConfigPopulatorUnitTest
{
	private static final String TEST_CODE = "testCode";
	private static final String TEST_NAME = "testName";

	private final Populator<StorageConfigModel, StorageConfigData> populator = new StorageConfigPopulator();

	private final StorageConfigModel source = mock(StorageConfigModel.class);
	private final StorageConfigData target = spy(new StorageConfigData());

	@Mock
	private Converter<StorageTypeModel, StorageTypeData> storageTypeConverter;

	@Before
	public void setUp()
	{
		initMocks(this);

		((StorageConfigPopulator) populator).setStorageTypeConverter(storageTypeConverter);
	}

	@Test
	public void populateTest()
	{
		when(source.getCode()).thenReturn(TEST_CODE);
		when(source.getName()).thenReturn(TEST_NAME);
		when(source.isUseSignedURLs()).thenReturn(true);
		when(source.getIsConnected()).thenReturn(true);
		when(storageTypeConverter.convert(new StorageTypeModel())).thenReturn(new StorageTypeData());

		populator.populate(source, target);

		assertEquals(TEST_CODE, target.getCode());
		assertEquals(TEST_NAME, target.getName());
		assertTrue(target.getIsConnected());
		assertTrue(target.isUseSignedUrls());
	}

	@Test
	public void sourceIsNullTest()
	{
		populator.populate(null, target);
		verifyZeroInteractions(target);
	}
}
