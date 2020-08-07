package com.aimprosoft.importexportcloud.exceptions;

import com.aimprosoft.importexportcloud.model.TaskInfoModel;


public class ExportException extends IemException
{
	public ExportException(final String message, final TaskInfoModel taskInfoModel)
	{
		super(message, taskInfoModel);
	}

	public ExportException(final Throwable cause, final TaskInfoModel taskInfoModel)
	{
		super(cause, taskInfoModel);
	}

	public ExportException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public ExportException(final String message, final Throwable cause, final TaskInfoModel taskInfoModel)
	{
		super(message, cause, taskInfoModel);
	}

	public ExportException(final String message)
	{
		super(message);
	}

}
