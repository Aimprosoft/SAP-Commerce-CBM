package com.aimprosoft.importexportcloud.converters.populators;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.StorageConfigModel;
import com.aimprosoft.importexportcloud.model.TaskInfoModel;
import com.aimprosoft.importexportcloud.service.StorageConfigService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.user.UserService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


@UnitTest
public class TaskInfoReversePopulatorUnitTest
{
	private static final String TEST_CODE = "testCode";
	private static final String STORAGE_CONFIG_DATA_CODE = "storageConfigDataCode";

	private final Populator<TaskInfoData, TaskInfoModel> populator = new TaskInfoReversePopulator();

	private final TaskInfoData source = mock(TaskInfoData.class);
	private final TaskInfoModel target = spy(new TaskInfoModel());

	@Mock
	private UserService userService;
	@Mock
	private UserModel user;
	@Mock
	private KeyGenerator keyGenerator;
	@Mock
	private StorageConfigService storageConfigService;
	@Mock
	private StorageConfigModel storageConfigModel;
	@Mock
	private StorageConfigData storageConfigData;

	@Before
	public void setUp()
	{
		initMocks(this);

		((TaskInfoReversePopulator) populator).setStorageConfigService(storageConfigService);
		((TaskInfoReversePopulator) populator).setKeyGenerator(keyGenerator);
		((TaskInfoReversePopulator) populator).setUserService(userService);
	}

	@Test
	public void populateTest()
	{
		when(userService.getCurrentUser()).thenReturn(user);
		when(keyGenerator.generate()).thenReturn(TEST_CODE);
		when(source.getConfig()).thenReturn(storageConfigData);
		when(storageConfigData.getCode()).thenReturn(STORAGE_CONFIG_DATA_CODE);
		when(storageConfigService.getStorageConfigByCode(STORAGE_CONFIG_DATA_CODE)).thenReturn(storageConfigModel);

		populator.populate(source, target);

		assertEquals(user, target.getUser());
		assertEquals(storageConfigModel, target.getStorageConfig());
		assertEquals(TEST_CODE, target.getCode());
		assertEquals(source.isExportMediaNeeded(), target.isExportMediaNeeded());
	}

	@Test
	public void sourceIsNullTest()
	{
		populator.populate(null, target);
		verifyZeroInteractions(target);
	}
}
