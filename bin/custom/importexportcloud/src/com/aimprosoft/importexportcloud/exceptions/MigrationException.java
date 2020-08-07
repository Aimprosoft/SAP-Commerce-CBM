package com.aimprosoft.importexportcloud.exceptions;

import com.aimprosoft.importexportcloud.model.TaskInfoModel;


public class MigrationException extends IemException
{
	public MigrationException(final Throwable cause)
	{
		super(cause);
	}

	public MigrationException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public MigrationException(final String message, final TaskInfoModel taskInfoModel)
	{
		super(message, taskInfoModel);
	}

	public MigrationException(final Throwable cause, final TaskInfoModel taskInfoModel)
	{
		super(cause, taskInfoModel);
	}

	public MigrationException(final String message, final Throwable cause, final TaskInfoModel taskInfoModel)
	{
		super(message, cause, taskInfoModel);
	}

	public MigrationException(final String message)
	{
		super(message);
	}
}
