package com.aimprosoft.importexportcloud.exceptions;

import com.aimprosoft.importexportcloud.model.TaskInfoModel;


public class ExportException extends IemException
{
	public ExportException(String message, TaskInfoModel taskInfoModel)
	{
		super(message, taskInfoModel);
	}

	public ExportException(Throwable cause, TaskInfoModel taskInfoModel)
	{
		super(cause, taskInfoModel);
	}

	public ExportException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ExportException(String message, Throwable cause, TaskInfoModel taskInfoModel)
	{
		super(message, cause, taskInfoModel);
	}

	public ExportException(String message)
	{
		super(message);
	}

}
