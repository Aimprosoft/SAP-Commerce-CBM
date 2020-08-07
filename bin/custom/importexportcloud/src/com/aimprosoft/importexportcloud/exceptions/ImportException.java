package com.aimprosoft.importexportcloud.exceptions;

import com.aimprosoft.importexportcloud.model.TaskInfoModel;


@SuppressWarnings("unused")
public class ImportException extends IemException
{
	public ImportException(final String message, final TaskInfoModel taskInfoModel)
	{
		super(message, taskInfoModel);
	}

	public ImportException(final Throwable cause, final TaskInfoModel taskInfoModel)
	{
		super(cause, taskInfoModel);
	}

	public ImportException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public ImportException(final String message, final Throwable cause, final TaskInfoModel taskInfoModel)
	{
		super(message, cause, taskInfoModel);
	}

	public ImportException(final String message)
	{
		super(message);
	}

}
