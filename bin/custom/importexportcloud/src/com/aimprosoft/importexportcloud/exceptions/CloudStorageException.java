package com.aimprosoft.importexportcloud.exceptions;

import com.aimprosoft.importexportcloud.model.TaskInfoModel;


public class CloudStorageException extends IemException
{
	public CloudStorageException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public CloudStorageException(final String message, final TaskInfoModel taskInfoModel)
	{
		super(message, taskInfoModel);
	}

	public CloudStorageException(final Throwable cause, final TaskInfoModel taskInfoModel)
	{
		super(cause, taskInfoModel);
	}

	public CloudStorageException(final String message, final Throwable cause, final TaskInfoModel taskInfoModel)
	{
		super(message, cause, taskInfoModel);
	}

	public CloudStorageException(final String message)
	{
		super(message);
	}
}
