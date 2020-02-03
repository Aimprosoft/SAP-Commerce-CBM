package com.aimprosoft.importexportcloud.service.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateIfSingleResult;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static java.lang.String.format;

import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.PaginationData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.internal.service.AbstractBusinessService;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.dao.TaskInfoPaginatedDao;
import com.aimprosoft.importexportcloud.enums.TaskInfoStatus;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import com.aimprosoft.importexportcloud.model.ImportTaskInfoModel;
import com.aimprosoft.importexportcloud.model.StorageTypeModel;
import com.aimprosoft.importexportcloud.model.TaskInfoModel;
import com.aimprosoft.importexportcloud.service.TaskInfoService;


public class DefaultTaskInfoService extends AbstractBusinessService implements TaskInfoService<TaskInfoModel>
{
	private static final int DEFAULT_PAGE_SIZE = 10;

	private TaskInfoPaginatedDao<TaskInfoModel> taskInfoPaginatedDao;

	private Converter<TaskInfoData, ExportTaskInfoModel> exportTaskInfoReverseConverter;

	private Converter<TaskInfoData, ImportTaskInfoModel> importTaskInfoReverseConverter;

	private KeyGenerator keyGenerator;

	@Override
	public SearchPageData<TaskInfoModel> getAllTasks(final SearchPageData<TaskInfoModel> searchPageData)
	{
		validateParameterNotNullStandardMessage("searchPageData", searchPageData);
		return taskInfoPaginatedDao.find(searchPageData);
	}

	@Override
	public TaskInfoModel getTaskByCode(final String code)
	{
		validateParameterNotNullStandardMessage(StorageTypeModel.CODE, code);

		final Map<String, Object> searchParameterMap = new HashMap<>();
		searchParameterMap.put(StorageTypeModel.CODE, code);

		final List<TaskInfoModel> storageTypeList = taskInfoPaginatedDao.find(searchParameterMap, getSearchPageData()).getResults();

		validateIfSingleResult(storageTypeList, format("StorageType with code '%s' not found!", code),
				format("StorageType code '%s' is not unique, %d storage types found!", code, storageTypeList.size()));

		return storageTypeList.get(0);
	}

	@Override
	public SearchPageData<TaskInfoModel> getTasksByUserAndType(final String userUid, final TaskInfoModel type,
			final SearchPageData<TaskInfoModel> searchPageData)
	{
		validateParameterNotNullStandardMessage(UserModel.UID, userUid);
		validateParameterNotNullStandardMessage("type", type);
		validateParameterNotNullStandardMessage("searchPageData", searchPageData);
		return taskInfoPaginatedDao.findByUserAndType(userUid, type, searchPageData);
	}

	@Override
	public ExportTaskInfoModel createExportTaskInfoModel(final TaskInfoData taskInfoData)
	{
		final ExportTaskInfoModel result = getModelService().create(ExportTaskInfoModel.class);
		exportTaskInfoReverseConverter.convert(taskInfoData, result);
		setTaskInfoStatus(result, TaskInfoStatus.EXPORTING);

		return result;
	}

	@Override
	public ImportTaskInfoModel createImportTaskInfoModel(final TaskInfoData taskInfoData)
	{
		final ImportTaskInfoModel result = getModelService().create(ImportTaskInfoModel.class);
		importTaskInfoReverseConverter.convert(taskInfoData, result);
		setTaskInfoStatus(result, TaskInfoStatus.DOWNLOADING);

		return result;
	}

	@Override
	public void setTaskInfoStatus(final TaskInfoModel taskInfoModel, final TaskInfoStatus taskInfoStatus)
	{
		taskInfoModel.setStatus(taskInfoStatus);
		getModelService().save(taskInfoModel);
	}

	@Override
	public void setTaskInfoFinishedDate(final TaskInfoModel taskInfoModel)
	{
		taskInfoModel.setFinishedDate(new Date());
		getModelService().save(taskInfoModel);
	}

	@Override
	public void setTaskInfoCronJob(final TaskInfoModel taskInfoModel, final CronJobModel cronJobModel)
	{
		taskInfoModel.setCronJob(cronJobModel);
		getModelService().save(taskInfoModel);
	}

	@Override
	public void updateTaskInfo(final TaskInfoModel taskInfoModel, final CronJobModel cronJobModel, final TaskInfoStatus taskInfoStatus)
	{
		taskInfoModel.setStatus(taskInfoStatus);
		taskInfoModel.setFinishedDate(new Date());
		taskInfoModel.setCronJob(cronJobModel);
		getModelService().save(taskInfoModel);
	}

	private SearchPageData<TaskInfoModel> getSearchPageData()
	{
		final SearchPageData<TaskInfoModel> searchPageData = new SearchPageData<>();
		final PaginationData paginationData = new PaginationData();
		paginationData.setCurrentPage(0);
		paginationData.setPageSize(DEFAULT_PAGE_SIZE);
		searchPageData.setPagination(paginationData);
		return searchPageData;
	}

	public TaskInfoPaginatedDao<TaskInfoModel> getTaskInfoPaginatedDao()
	{
		return taskInfoPaginatedDao;
	}

	@Required
	public void setTaskInfoPaginatedDao(final TaskInfoPaginatedDao<TaskInfoModel> taskInfoPaginatedDao)
	{
		this.taskInfoPaginatedDao = taskInfoPaginatedDao;
	}

	public Converter<TaskInfoData, ImportTaskInfoModel> getImportTaskInfoReverseConverter()
	{
		return importTaskInfoReverseConverter;
	}

	@Required
	public void setImportTaskInfoReverseConverter(final Converter<TaskInfoData, ImportTaskInfoModel> importTaskInfoReverseConverter)
	{
		this.importTaskInfoReverseConverter = importTaskInfoReverseConverter;
	}

	public Converter<TaskInfoData, ExportTaskInfoModel> getExportTaskInfoReverseConverter()
	{
		return exportTaskInfoReverseConverter;
	}

	@Required
	public void setExportTaskInfoReverseConverter(final Converter<TaskInfoData, ExportTaskInfoModel> exportTaskInfoReverseConverter)
	{
		this.exportTaskInfoReverseConverter = exportTaskInfoReverseConverter;
	}

	public KeyGenerator getKeyGenerator()
	{
		return keyGenerator;
	}

	@Required
	public void setKeyGenerator(final KeyGenerator keyGenerator)
	{
		this.keyGenerator = keyGenerator;
	}
}
