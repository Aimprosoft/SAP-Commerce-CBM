package com.aimprosoft.importexportcloud.exceptions;

import com.aimprosoft.importexportcloud.model.TaskInfoModel;


@SuppressWarnings("unused")
public class ImportException extends IemException
{
	public ImportException(String message, TaskInfoModel taskInfoModel)
	{
		super(message, taskInfoModel);
	}

	public ImportException(Throwable cause, TaskInfoModel taskInfoModel)
	{
		super(cause, taskInfoModel);
	}

	public ImportException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ImportException(String message, Throwable cause, TaskInfoModel taskInfoModel)
	{
		super(message, cause, taskInfoModel);
	}

	public ImportException(String message)
	{
		super(message);
	}

}
