package com.aimprosoft.importexportcloud.converters.populators;

import com.aimprosoft.importexportcloud.enums.TaskInfoScope;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import com.aimprosoft.importexportcloud.service.IemCMSSiteService;
import com.aimprosoft.importexportcloud.service.IemCatalogVersionService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.enumeration.EnumerationService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static de.hybris.platform.testframework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


@UnitTest
public class ExportTaskInfoReversePopulatorUnitTest
{
	private static final String SITE_UID = "testSiteUID";
	private static final String TEST_CATALOG = "testCatalog";
	private static final String TEST_FOLDER_FOR_DISPLAY = "testFolderForDisplay";
	private static final String TEST_RESULT_PREFIX = "testResultPrefix";
	private static final String TARGET_CODE = "targetCode";

	private final Populator<TaskInfoData, ExportTaskInfoModel> populator = new ExportTaskInfoReversePopulator();

	private final TaskInfoData source = mock(TaskInfoData.class);
	private final ExportTaskInfoModel target = spy(new ExportTaskInfoModel());

	@Mock
	private IemCMSSiteService iemCmsSiteService;
	@Mock
	private IemCatalogVersionService iemCatalogVersionService;
	@Mock
	private EnumerationService enumerationService;
	@Mock
	private CMSSiteModel cmsSite;
	@Mock
	private CatalogVersionModel catalogVersion;

	@Before
	public void setUp()
	{
		initMocks(this);

		((ExportTaskInfoReversePopulator) populator).setEnumerationService(enumerationService);
		((ExportTaskInfoReversePopulator) populator).setIemCatalogVersionService(iemCatalogVersionService);
		((ExportTaskInfoReversePopulator) populator).setIemCmsSiteService(iemCmsSiteService);
	}

	@Test
	public void populateTest()
	{
		when(source.getCmsSiteUid()).thenReturn(SITE_UID);
		when(iemCmsSiteService.getCMSSiteForUid(SITE_UID)).thenReturn(cmsSite);
		when(source.getCatalogIdAndVersionName()).thenReturn(TEST_CATALOG);
		when(iemCatalogVersionService.getCatalogVersionModelWithoutSearchRestrictions(TEST_CATALOG)).thenReturn(catalogVersion);
		when(source.getTaskInfoScopeCode()).thenReturn("siteScope");
		when(enumerationService.getEnumerationValue(TaskInfoScope.class, source.getTaskInfoScopeCode()))
				.thenReturn(TaskInfoScope.SITESCOPE);
		when(source.getCloudUploadFolderPathToDisplay()).thenReturn(TEST_FOLDER_FOR_DISPLAY);
		when(source.getResultPrefix()).thenReturn(TEST_RESULT_PREFIX);
		when(target.getCode()).thenReturn(TARGET_CODE);
		when(target.isExportMediaNeeded()).thenReturn(true);

		populator.populate(source, target);

		assertEquals(TEST_FOLDER_FOR_DISPLAY + "/" + TEST_RESULT_PREFIX + "_" + TARGET_CODE + ".zip", target.getExternalPath());
		assertEquals(TaskInfoScope.SITESCOPE, target.getTaskScope());
		assertEquals(cmsSite, target.getSite());
		assertEquals(catalogVersion, target.getCatalogVersion());

		assertTrue(target.isExportMediaNeeded());
	}

	@Test
	public void sourceIsNullTest()
	{
		populator.populate(null, target);
		verifyZeroInteractions(target);
	}
}
