package com.aimprosoft.importexportcloud.facades;

import com.aimprosoft.importexportcloud.exceptions.ImportException;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;


public interface IemImportFacade
{
	/**
	 * Performs importing of a specified zip file.
	 *
	 * @param taskInfoData task information DTO
	 * @return task information DTO
	 * @throws ImportException if import process fails
	 */
	TaskInfoData importData(TaskInfoData taskInfoData) throws ImportException;
}
