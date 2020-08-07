package com.aimprosoft.importexportcloud.converters.populators;

import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.MigrationTaskInfoModel;
import com.aimprosoft.importexportcloud.model.StorageConfigModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.user.UserService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


@UnitTest
public class MigrationTaskInfoReversePopulatorUnitTest
{
	private static final String GENERATED_KEY = "generatedKey";

	private final Populator<TaskInfoData, MigrationTaskInfoModel> populator = new MigrationTaskInfoReversePopulator();

	private final TaskInfoData source = mock(TaskInfoData.class);
	private final MigrationTaskInfoModel target = spy(new MigrationTaskInfoModel());

	@Mock
	private MediaService mediaService;
	@Mock
	private KeyGenerator keyGenerator;
	@Mock
	private StorageConfigFacade storageConfigFacade;
	@Mock
	private UserService userService;
	@Mock
	private StorageConfigData storageConfigData;
	@Mock
	private StorageConfigModel storageConfigModel;
	@Mock
	private MediaFolderModel mediaFolder;

	@Before
	public void setUp()
	{
		initMocks(this);

		((MigrationTaskInfoReversePopulator) populator).setMediaService(mediaService);
		((MigrationTaskInfoReversePopulator) populator).setKeyGenerator(keyGenerator);
		((MigrationTaskInfoReversePopulator) populator).setStorageConfigFacade(storageConfigFacade);
		((MigrationTaskInfoReversePopulator) populator).setUserService(userService);
	}

	@Test
	public void targetConfigDataIsNull()
	{
		when(source.getConfig()).thenReturn(null);

		populator.populate(source, target);

		assertNull(target.getStorageConfig());
	}

	@Test
	public void populateTest()
	{
		when(source.getMediaFolderQualifier()).thenReturn("testMediaFolder");
		when(mediaService.getFolder(source.getMediaFolderQualifier())).thenReturn(mediaFolder);
		when(source.getSourceConfigCode()).thenReturn("testSourceConfigCode");
		when(source.getConfig()).thenReturn(storageConfigData);
		when(storageConfigData.getCode()).thenReturn("testStorageConfigDataCode");
		when(keyGenerator.generate()).thenReturn(GENERATED_KEY);
		when(storageConfigFacade.getStorageConfigModelByCode(source.getSourceConfigCode())).thenReturn(storageConfigModel);
		when(storageConfigFacade.getStorageConfigModelByCode(storageConfigData.getCode())).thenReturn(storageConfigModel);

		populator.populate(source, target);

		assertEquals(storageConfigModel, target.getSourceStorageConfig());
		assertEquals(mediaFolder, target.getMediaFolder());
		assertEquals(GENERATED_KEY, target.getCode());
		assertEquals(storageConfigModel, target.getStorageConfig());
		assertEquals(userService.getAdminUser(), target.getUser());
	}

	@Test
	public void sourceIsNullTest()
	{
		populator.populate(null, target);
		verifyZeroInteractions(target);
	}
}
