package com.aimprosoft.importexportcloud.exceptions;

import com.aimprosoft.importexportcloud.model.TaskInfoModel;


public class AbortMigrationException extends MigrationException
{
	public AbortMigrationException(final Throwable cause)
	{
		super(cause);
	}

	public AbortMigrationException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public AbortMigrationException(final String message, final TaskInfoModel taskInfoModel)
	{
		super(message, taskInfoModel);
	}

	public AbortMigrationException(final Throwable cause, final TaskInfoModel taskInfoModel)
	{
		super(cause, taskInfoModel);
	}

	public AbortMigrationException(final String message, final Throwable cause, final TaskInfoModel taskInfoModel)
	{
		super(message, cause, taskInfoModel);
	}

	public AbortMigrationException(final String message)
	{
		super(message);
	}
}
