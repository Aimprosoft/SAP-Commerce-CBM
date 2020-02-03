package com.aimprosoft.importexportcloud.exceptions;

import com.aimprosoft.importexportcloud.model.TaskInfoModel;


public class CloudStorageException extends IemException
{
	public CloudStorageException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public CloudStorageException(String message, TaskInfoModel taskInfoModel)
	{
		super(message, taskInfoModel);
	}

	public CloudStorageException(Throwable cause, TaskInfoModel taskInfoModel)
	{
		super(cause, taskInfoModel);
	}

	public CloudStorageException(String message, Throwable cause, TaskInfoModel taskInfoModel)
	{
		super(message, cause, taskInfoModel);
	}

	public CloudStorageException(String message)
	{
		super(message);
	}
}
