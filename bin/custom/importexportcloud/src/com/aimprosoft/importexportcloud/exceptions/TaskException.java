package com.aimprosoft.importexportcloud.exceptions;

import com.aimprosoft.importexportcloud.model.TaskInfoModel;


public class TaskException extends IemException
{
	public TaskException(final Throwable cause)
	{
		super(cause);
	}

	public TaskException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public TaskException(final String message, final TaskInfoModel taskInfoModel)
	{
		super(message, taskInfoModel);
	}

	public TaskException(final Throwable cause, final TaskInfoModel taskInfoModel)
	{
		super(cause, taskInfoModel);
	}

	public TaskException(final String message, final Throwable cause,
			final TaskInfoModel taskInfoModel)
	{
		super(message, cause, taskInfoModel);
	}

	public TaskException(final String message)
	{
		super(message);
	}
}
