package com.aimprosoft.importexportcloud.exceptions;

public class InvalidTokenException extends IemException
{
	public InvalidTokenException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public InvalidTokenException(Throwable cause)
	{
		super(cause);
	}

	public InvalidTokenException(String message)
	{
		super(message);
	}
}
