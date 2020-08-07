package com.aimprosoft.importexportcloud.facades;

import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;


public interface ExportFacade
{
	/**
	 * Performs exporting of specified data.
	 *
	 * @param taskInfoData task information DTO
	 * @return task information DTO with result exported zip file assigned
	 * @throws ExportException if export process fails
	 */
	TaskInfoData exportData(TaskInfoData taskInfoData) throws ExportException;
}
