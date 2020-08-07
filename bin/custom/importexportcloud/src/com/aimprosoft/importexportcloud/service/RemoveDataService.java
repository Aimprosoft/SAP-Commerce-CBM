package com.aimprosoft.importexportcloud.service;

import com.aimprosoft.importexportcloud.exceptions.TaskException;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;


public interface RemoveDataService
{
	void removeData(TaskInfoData taskInfoData) throws TaskException;
}
