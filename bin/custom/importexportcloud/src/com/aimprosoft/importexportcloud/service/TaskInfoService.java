package com.aimprosoft.importexportcloud.service;

import com.aimprosoft.importexportcloud.enums.TaskInfoStatus;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import com.aimprosoft.importexportcloud.model.ImportTaskInfoModel;
import com.aimprosoft.importexportcloud.model.TaskInfoModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;


public interface TaskInfoService<T extends TaskInfoModel>
{
	/**
	 * Gets pageable search data with a list of task info models.
	 * If none are found search pageable data with an empty list is returned.
	 *
	 * @param searchPageData pageable search data to identify the current pagination state
	 * @return pageable list of task info models
	 * @throws IllegalArgumentException in case searchPageData is null
	 */
	SearchPageData<T> getAllTasks(SearchPageData<T> searchPageData);

	/**
	 * Gets the task info for the given code.
	 *
	 * @param code the code to search for task info
	 * @return task info for code
	 * @throws de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException   in case no task info for the given code can be found
	 * @throws de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException in case more than one task info is found for the given code
	 */
	T getTaskByCode(String code);

	/**
	 * Gets the task info for the given user uid.
	 *
	 * @param userUid        to search for task info by user
	 * @param type           instance of task info to search
	 * @param searchPageData pageable search data to identify the current pagination state
	 * @return task info search page data for given user and types, null if not found
	 * @throws IllegalArgumentException in case userUid is null
	 * @throws IllegalArgumentException in case searchPageData is null
	 * @see com.aimprosoft.importexportcloud.model.TaskInfoModel
	 * @see com.aimprosoft.importexportcloud.model.ImportTaskInfoModel
	 * @see com.aimprosoft.importexportcloud.model.ExportTaskInfoModel
	 */
	SearchPageData<T> getTasksByUserAndType(String userUid, T type, SearchPageData<T> searchPageData);

	/**
	 * Creates and persists export task info model from storage configuration data.
	 *
	 * @param taskInfoData   task information
	 * @return persisted export task info model
	 * @throws ConversionException if combination of task scope, site and catalog version is not valid
	 */
	ExportTaskInfoModel createExportTaskInfoModel(TaskInfoData taskInfoData);

	/**
	 * Creates and persists import task info model from storage configuration data.
	 *
	 * @param taskInfoData   task information
	 * @return persisted import task info model
	 */
	ImportTaskInfoModel createImportTaskInfoModel(TaskInfoData taskInfoData);

	/**
	 * Sets new status and saves task info model
	 *
	 * @param taskInfoModel  task info to change status
	 * @param taskInfoStatus status to set
	 */
	void setTaskInfoStatus(TaskInfoModel taskInfoModel, TaskInfoStatus taskInfoStatus);

	/**
	 * Sets current date and saves task info model.
	 *
	 * @param taskInfoModel task info to set finished date
	 */
	void setTaskInfoFinishedDate(TaskInfoModel taskInfoModel);

	/**
	 * Sets cronjob and saves task info model.
	 *
	 * @param taskInfoModel task info to set finished date
	 * @param cronJobModel  cronjob to set
	 */
	void setTaskInfoCronJob(TaskInfoModel taskInfoModel, CronJobModel cronJobModel);

	/**
	 * Sets cronjob and saves task info model.
	 *
	 * @param taskInfoModel task info to set finished date
	 * @param cronJobModel  cronjob to set
	 * @param taskInfoStatus status to set
	 */
	void updateTaskInfo(TaskInfoModel taskInfoModel, CronJobModel cronJobModel, TaskInfoStatus taskInfoStatus);
}
