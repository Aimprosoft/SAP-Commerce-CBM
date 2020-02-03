package com.aimprosoft.importexportcloud.exceptions;

import com.aimprosoft.importexportcloud.model.TaskInfoModel;
import de.hybris.platform.servicelayer.exceptions.BusinessException;


public class IemException extends BusinessException
{
	private TaskInfoModel taskInfoModel;

	public IemException(Throwable cause)
	{
		super(cause);
	}

	public IemException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public IemException(String message, TaskInfoModel taskInfoModel)
	{
		super(message);
		this.taskInfoModel = taskInfoModel;
	}

	public IemException(Throwable cause, TaskInfoModel taskInfoModel)
	{
		super(cause);
		this.taskInfoModel = taskInfoModel;
	}

	public IemException(String message, Throwable cause, TaskInfoModel taskInfoModel)
	{
		super(message, cause);
		this.taskInfoModel = taskInfoModel;
	}

	public IemException(String message)
	{
		super(message);
	}

	public TaskInfoModel getTaskInfoModel()
	{
		return taskInfoModel;
	}
}
