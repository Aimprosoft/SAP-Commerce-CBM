package com.aimprosoft.importexportcloud.service.impl;

import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import com.aimprosoft.importexportcloud.model.ImportTaskInfoModel;
import com.aimprosoft.importexportcloud.model.TaskInfoModel;
import com.aimprosoft.importexportcloud.service.TaskInfoService;
import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.core.servicelayer.data.PaginationData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@IntegrationTest
public class DefaultTaskInfoServiceIntegrationTest extends ServicelayerTransactionalTest
{
	private static final String ENCODING = "UTF-8";
	private static final int PAGE_SIZE = 20;
	private static final int PAGE_NUMBER = 0;
	private static final String IMPORT_TASK_PREFIX = "import_task";
	private static final String EXPORT_TASK_PREFIX = "export_task";
	private static final int TASKS_COUNT = 20;
	private static final int TASK_TYPE_COUNT = 2;
	private static final String USER_UID = "testUser";

	@Resource
	private TaskInfoService<TaskInfoModel> taskInfoService;

	private SearchPageData<TaskInfoModel> searchPageData;

	@Before
	public void setUp() throws Exception
	{
		createCoreData();
		importCsv("/test/test-user.impex", ENCODING);
		importCsv("/test/test-storage-configs.impex", ENCODING);
		importCsv("/test/test-task-info.impex", ENCODING);
		searchPageData = createSearchPageData();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetAllTasksNullSearchPageData()
	{
		taskInfoService.getAllTasks(null);
	}

	@Test
	public void testGetAllTasksShouldReturnAllItems()
	{
		SearchPageData<TaskInfoModel> tasks = taskInfoService.getAllTasks(searchPageData);
		Assert.assertTrue("Task list is empty", CollectionUtils.isNotEmpty(tasks.getResults()));
		Assert.assertEquals("Not all tasks are found", TASKS_COUNT, tasks.getResults().size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetAllTasksByUserAndTypeNullUser()
	{
		taskInfoService.getTasksByUserAndType(null, new ImportTaskInfoModel(), searchPageData);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetAllTasksByUserAndTypeNullType()
	{
		taskInfoService.getTasksByUserAndType(USER_UID, null, searchPageData);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetAllTasksByUserAndTypeNullSearchPageData()
	{
		taskInfoService.getTasksByUserAndType(USER_UID, new ImportTaskInfoModel(), null);
	}

	@Test
	public void testGetAllTasks()
	{
		SearchPageData<TaskInfoModel> searchResult = taskInfoService.getAllTasks(searchPageData);
		Assert.assertNotNull("Search result was null", searchResult);
		List<TaskInfoModel> tasks = searchResult.getResults();
		Assert.assertEquals("List contains not all the tasks", CollectionUtils.size(tasks), PAGE_SIZE);
	}

	@Test
	public void testGetAllTasksByTypeAndUserPagination()
	{
		SearchPageData<TaskInfoModel> smallSearchPageData = createSearchPageData();
		int pageSize = PAGE_SIZE / 2;
		smallSearchPageData.getPagination().setPageSize(pageSize);

		SearchPageData<TaskInfoModel> firstPage = taskInfoService.getAllTasks(smallSearchPageData);
		Assert.assertEquals("First page results wrong count", CollectionUtils.size(firstPage.getResults()), pageSize);

		smallSearchPageData.getPagination().setCurrentPage(1);
		SearchPageData<TaskInfoModel> secondPage = taskInfoService.getAllTasks(smallSearchPageData);
		Assert.assertEquals("Second page results wrong count", CollectionUtils.size(secondPage.getResults()), pageSize);
	}

	@Test
	public void testGetAllTasksByTypeAndUser()
	{
		List<String> expectedCodes = createTaskCodes(IMPORT_TASK_PREFIX);
		expectedCodes.addAll(createTaskCodes(EXPORT_TASK_PREFIX));
		testGetTasksByTypeAndUser(new TaskInfoModel(), expectedCodes);
	}

	@Test
	public void testGetAllImportTasksByTypeAndUser()
	{
		List<String> expectedCodes = createTaskCodes(IMPORT_TASK_PREFIX);
		testGetTasksByTypeAndUser(new ImportTaskInfoModel(), expectedCodes);

	}

	@Test
	public void testGetAllExportTasksByTypeAndUser()
	{
		List<String> expectedCodes = createTaskCodes(EXPORT_TASK_PREFIX);
		testGetTasksByTypeAndUser(new ExportTaskInfoModel(), expectedCodes);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetTaskByCodeNullCode()
	{
		taskInfoService.getTaskByCode(null);
	}

	@Test
	public void testGetTaskByCode()
	{
		String code = EXPORT_TASK_PREFIX + "_" + 1;
		TaskInfoModel task = taskInfoService.getTaskByCode(code);
		Assert.assertNotNull("Null task info returned", task);
		Assert.assertEquals("Wrong task code retured", task.getCode(), code);
	}

	private <T extends TaskInfoModel> void testGetTasksByTypeAndUser(final T type, final List<String> expectedCodes)
	{
		SearchPageData<TaskInfoModel> searchResult = taskInfoService.getTasksByUserAndType(USER_UID, type, searchPageData);
		List<TaskInfoModel> tasks = searchResult.getResults();
		Assert.assertEquals("Invalid tasks list size", CollectionUtils.size(tasks), expectedCodes.size());
		Assert.assertTrue("Invalid tasks list content", taskListMatches(tasks, expectedCodes));
	}

	private <T extends TaskInfoModel> boolean taskListMatches(final List<T> tasks, final List<String> expectedCodes)
	{
		return tasks.stream()
				.map(TaskInfoModel::getCode)
				.allMatch(expectedCodes::contains);
	}

	private SearchPageData<TaskInfoModel> createSearchPageData()
	{
		SearchPageData<TaskInfoModel> searchPageData = new SearchPageData<>();
		PaginationData paginationData = new PaginationData();
		paginationData.setPageSize(PAGE_SIZE);
		paginationData.setCurrentPage(PAGE_NUMBER);
		searchPageData.setPagination(paginationData);
		return searchPageData;
	}

	private List<String> createTaskCodes(String taskPrefix)
	{
		return IntStream.range(1, TASKS_COUNT / TASK_TYPE_COUNT + 1)
				.mapToObj(Integer::toString)
				.map(value -> taskPrefix + "_" + value)
				.collect(Collectors.toList());
	}
}
