package com.aimprosoft.importexportcloud.converters.populators;

import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.data.MediaFolderData;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.model.StorageConfigModel;
import com.aimprosoft.importexportcloud.model.StorageTypeModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.media.storage.MediaStorageConfigService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


@UnitTest
public class DefaultMediaFolderPopulatorUnitTest
{
	private static final String TEST_QUALIFIER = "testQualifier";
	private static final String STORAGE_CONFIG_CODE = "storageConfigCode";
	private static final String STORAGE_CONFIG_NAME = "storageConfigName";
	private static final String STORAGE_TYPE_NAME = "typeName";
	private static final String TEST_STORAGE_STRATEGY_ID = "remoteFileMediaStorageStrategy";
	private static final String STORAGE_CONFIG_DATA_NAME = "storageConfigDataName";
	private static final String LOCAL_STORAGE_CONFIG = "localStorageConfig";

	private final Populator<MediaFolderModel, MediaFolderData> populator = new DefaultMediaFolderPopulator();

	private final MediaFolderModel source = mock(MediaFolderModel.class);
	private final MediaFolderData target = spy(new MediaFolderData());

	@Mock
	private MediaStorageConfigService mediaStorageConfigService;
	@Mock
	private StorageConfigFacade storageConfigFacade;
	@Mock
	private StorageConfigModel storageConfigModel;
	@Mock
	private StorageTypeModel storageTypeModel;
	@Mock
	private MediaStorageConfigService.MediaFolderConfig mediaFolderConfig;
	@Mock
	private StorageConfigData storageConfigData;

	@Before
	public void setUp()
	{
		initMocks(this);

		((DefaultMediaFolderPopulator) populator).setMediaStorageConfigService(mediaStorageConfigService);
		((DefaultMediaFolderPopulator) populator).setStorageConfigFacade(storageConfigFacade);

		when(source.getQualifier()).thenReturn(TEST_QUALIFIER);
		when(source.isCanMigrate()).thenReturn(true);

		when(mediaStorageConfigService.getConfigForFolder(TEST_QUALIFIER)).thenReturn(mediaFolderConfig);
		when(mediaFolderConfig.getStorageStrategyId()).thenReturn(TEST_STORAGE_STRATEGY_ID);
	}

	@Test
	public void storageConfigModelIsNullTest()
	{
		when(source.getCurrentStorageConfig()).thenReturn(null);
		when(mediaStorageConfigService.getConfigForFolder(TEST_QUALIFIER)).thenReturn(mediaFolderConfig);
		when(mediaFolderConfig.getStorageStrategyId()).thenReturn(TEST_STORAGE_STRATEGY_ID);

		populator.populate(source, target);

		assertEquals("Local storage", target.getStorageTypeName());
		assertEquals(LOCAL_STORAGE_CONFIG, target.getStorageConfigCode());
		assertEquals(TEST_STORAGE_STRATEGY_ID, target.getStorageConfigName());
	}

	@Test
	public void storageIsLocalTest()
	{
		when(source.getCurrentStorageConfig()).thenReturn(null);
		when(mediaFolderConfig.getStorageStrategyId()).thenReturn("localFileMediaStorageStrategy");
		when(storageConfigFacade.getStorageConfigData(LOCAL_STORAGE_CONFIG)).thenReturn(storageConfigData);
		when(storageConfigData.getName()).thenReturn(STORAGE_CONFIG_DATA_NAME);

		populator.populate(source, target);

		assertEquals(STORAGE_CONFIG_DATA_NAME, target.getStorageConfigName());
	}

	@Test(expected = NullPointerException.class)
	public void sourceIsNullTest()
	{
		populator.populate(null, target);
	}

	@Test
	public void populateTest()
	{
		when(source.getCurrentStorageConfig()).thenReturn(storageConfigModel);
		when(storageConfigModel.getCode()).thenReturn(STORAGE_CONFIG_CODE);
		when(storageConfigModel.getName()).thenReturn(STORAGE_CONFIG_NAME);
		when(storageConfigModel.getType()).thenReturn(storageTypeModel);
		when(storageTypeModel.getName()).thenReturn(STORAGE_TYPE_NAME);

		populator.populate(source, target);

		assertEquals(STORAGE_TYPE_NAME, target.getStorageTypeName());
		assertEquals(STORAGE_CONFIG_CODE, target.getStorageConfigCode());
		assertEquals(STORAGE_CONFIG_NAME, target.getStorageConfigName());
	}
}
