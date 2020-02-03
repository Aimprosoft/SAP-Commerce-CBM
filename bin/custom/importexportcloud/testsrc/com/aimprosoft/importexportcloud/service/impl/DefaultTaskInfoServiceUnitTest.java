package com.aimprosoft.importexportcloud.service.impl;

import com.aimprosoft.importexportcloud.dao.TaskInfoPaginatedDao;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import com.aimprosoft.importexportcloud.model.ImportTaskInfoModel;
import com.aimprosoft.importexportcloud.model.TaskInfoModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@UnitTest
public class DefaultTaskInfoServiceUnitTest
{
	private static final String USER_UID = "testUid";

	private DefaultTaskInfoService taskInfoService;
	private TaskInfoPaginatedDao taskInfoPaginatedDao;

	@Before
	public void setUp()
	{
		taskInfoService = new DefaultTaskInfoService();
		taskInfoPaginatedDao = mock(TaskInfoPaginatedDao.class);
		taskInfoService.setTaskInfoPaginatedDao(taskInfoPaginatedDao);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetAllTasksNullSearchPageData()
	{
		when(taskInfoPaginatedDao.find(any(SearchPageData.class))).thenReturn(new SearchPageData());
		taskInfoService.getAllTasks(null);
	}

	@Test
	public void testGetAllTasks()
	{
		SearchPageData<TaskInfoModel> expected = new SearchPageData<>();
		expected.setResults(new ArrayList<>());
		when(taskInfoPaginatedDao.find(any(SearchPageData.class))).thenReturn(expected);
		SearchPageData<TaskInfoModel> actual = taskInfoService.getAllTasks(new SearchPageData<>());
		assertNotNull("Search page data was null", actual);
		assertTrue("Search page data has unexpected elements",
				CollectionUtils.isEqualCollection(actual.getResults(), expected.getResults()));

	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetTaskByCodeNullCode()
	{
		when(taskInfoPaginatedDao.find(anyMap(), any(SearchPageData.class))).thenReturn(new SearchPageData());
		taskInfoService.getTaskByCode(null);
	}

	@Test
	public void testGetTaskByCode()
	{
		SearchPageData<TaskInfoModel> expected = new SearchPageData<>();
		TaskInfoModel expectedTask = new TaskInfoModel();
		expected.setResults(Collections.singletonList(expectedTask));

		when(taskInfoPaginatedDao.find(anyMap(), any(SearchPageData.class))).thenReturn(expected);
		TaskInfoModel actualTask = taskInfoService.getTaskByCode(EMPTY);
		assertNotNull("Null task returned", actualTask);
		assertEquals("Unexpected task info returned", actualTask, expectedTask);
	}


	@Test(expected = IllegalArgumentException.class)
	public void testGetTasksByUserAndTypeNullUser()
	{
		when(taskInfoPaginatedDao.findByUserAndType(anyString(), any(TaskInfoModel.class), any(SearchPageData.class)))
				.thenReturn(new SearchPageData());
		taskInfoService.getTasksByUserAndType(null, new TaskInfoModel(), new SearchPageData<>());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetTasksByUserAndTypeNullType()
	{
		when(taskInfoPaginatedDao.findByUserAndType(anyString(), any(TaskInfoModel.class), any(SearchPageData.class)))
				.thenReturn(new SearchPageData());
		taskInfoService.getTasksByUserAndType(USER_UID, null, new SearchPageData<>());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetTasksByUserAndTypeNullPageData()
	{
		when(taskInfoPaginatedDao.findByUserAndType(anyString(), any(TaskInfoModel.class), any(SearchPageData.class)))
				.thenReturn(new SearchPageData());
		taskInfoService.getTasksByUserAndType(USER_UID, new TaskInfoModel(), null);
	}

	@Test
	public void testGetAllTasksByTypeAndUser()
	{
		testGetTasksByTypeAndUser(new TaskInfoModel());
	}

	@Test
	public void testGetExportTasksByTypeAndUser()
	{
		testGetTasksByTypeAndUser(new ExportTaskInfoModel());
	}

	@Test
	public void testGetImportTasksByTypeAndUser()
	{
		testGetTasksByTypeAndUser(new ImportTaskInfoModel());
	}


	private <T extends TaskInfoModel> void testGetTasksByTypeAndUser(T type)
	{
		SearchPageData<T> expected = createSearchPageData(type);
		when(taskInfoPaginatedDao.findByUserAndType(anyString(), any(type.getClass()), any(SearchPageData.class)))
				.thenReturn(expected);
		SearchPageData<TaskInfoModel> actual = taskInfoService
				.getTasksByUserAndType(USER_UID, type, new SearchPageData<>());

		assertFalse("List is empty for " + type.getItemtype(), CollectionUtils.isEmpty(actual.getResults()));
		assertTrue("Search data has unexpected elements for " + type.getItemtype(),
				CollectionUtils.isEqualCollection(actual.getResults(), expected.getResults()));
	}

	private <T extends TaskInfoModel> SearchPageData<T> createSearchPageData(T type)
	{
		SearchPageData<T> expected = new SearchPageData<>();
		expected.setResults(Collections.singletonList(type));
		return expected;

	}


}
