package com.aimprosoft.importexportcloud.exceptions;

import com.aimprosoft.importexportcloud.model.TaskInfoModel;
import de.hybris.platform.servicelayer.exceptions.BusinessException;


public class IemException extends BusinessException
{
	private TaskInfoModel taskInfoModel;

	public IemException(final Throwable cause)
	{
		super(cause);
	}

	public IemException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public IemException(final String message, final TaskInfoModel taskInfoModel)
	{
		super(message);
		this.taskInfoModel = taskInfoModel;
	}

	public IemException(final Throwable cause, final TaskInfoModel taskInfoModel)
	{
		super(cause);
		this.taskInfoModel = taskInfoModel;
	}

	public IemException(final String message, final Throwable cause, final TaskInfoModel taskInfoModel)
	{
		super(message, cause);
		this.taskInfoModel = taskInfoModel;
	}

	public IemException(final String message)
	{
		super(message);
	}

	public TaskInfoModel getTaskInfoModel()
	{
		return taskInfoModel;
	}
}
