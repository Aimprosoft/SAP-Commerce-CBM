package com.aimprosoft.importexportcloud.converters.populators;

import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;
import com.aimprosoft.importexportcloud.model.StorageTypeModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.converters.Populator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


@UnitTest
public class StorageTypePopulatorUnitTest
{
	private static final String MOCK_CODE = "mockCode";
	private static final String MOCK_NAME = "mockName";

	private final Populator<StorageTypeModel, StorageTypeData> populator = new StorageTypePopulator();

	private final StorageTypeModel source = mock(StorageTypeModel.class);
	private final StorageTypeData target = spy(new StorageTypeData());

	@Before
	public void setUp()
	{
		initMocks(this);
	}

	@Test
	public void populateTest()
	{
		when(source.getCode()).thenReturn(MOCK_CODE);
		when(source.getName()).thenReturn(MOCK_NAME);
		when(source.getLocal()).thenReturn(true);
		when(source.getDefault()).thenReturn(true);
		when(source.getAuthNeeded()).thenReturn(true);

		populator.populate(source, target);

		assertEquals(MOCK_CODE, target.getCode());
		assertEquals(MOCK_NAME, target.getName());
		assertTrue(target.getIsLocal());
		assertTrue(target.getIsAuthNeeded());
		assertTrue(target.getIsDefault());
	}

	@Test
	public void sourceIsNullTest()
	{
		populator.populate(null, target);
		verifyZeroInteractions(target);
	}
}
