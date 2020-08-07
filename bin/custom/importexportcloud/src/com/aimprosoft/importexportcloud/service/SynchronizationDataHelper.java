package com.aimprosoft.importexportcloud.service;

import com.aimprosoft.importexportcloud.exceptions.ImportException;
import com.aimprosoft.importexportcloud.exceptions.TaskException;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;


public interface SynchronizationDataHelper
{

	/**
	 * Synchronization imported data .
	 *
	 * @param taskInfoData task information
	 */
	void synchronizeData(TaskInfoData taskInfoData) throws ImportException, TaskException;
}
