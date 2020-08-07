package com.aimprosoft.importexportcloud.converters.populators;

import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.ImportTaskInfoModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.converters.Populator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


@UnitTest
public class ImportTaskInfoReversePopulatorUnitTest
{

	private static final String TEST_PATH = "test Path";

	private final Populator<TaskInfoData, ImportTaskInfoModel> populator = new ImportTaskInfoReversePopulator();

	private final TaskInfoData source = mock(TaskInfoData.class);
	private final ImportTaskInfoModel target = spy(new ImportTaskInfoModel());

	@Before
	public void setUp()
	{
		initMocks(this);
	}

	@Test
	public void populateTest()
	{
		when(source.getCloudFileDownloadPathToDisplay()).thenReturn(TEST_PATH);

		populator.populate(source, target);

		assertEquals(TEST_PATH, target.getExternalPath());
	}

	@Test
	public void sourceIsNullTest()
	{
		populator.populate(null, target);
		verifyZeroInteractions(target);
	}
}
