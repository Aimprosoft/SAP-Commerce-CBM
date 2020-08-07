package com.aimprosoft.importexportcloud.exceptions;

public class InvalidTokenException extends IemException
{
	public InvalidTokenException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public InvalidTokenException(final Throwable cause)
	{
		super(cause);
	}

	public InvalidTokenException(final String message)
	{
		super(message);
	}
}
