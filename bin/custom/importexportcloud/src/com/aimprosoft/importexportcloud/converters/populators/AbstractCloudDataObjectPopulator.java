package com.aimprosoft.importexportcloud.converters.populators;

import static com.aimprosoft.importexportcloud.constants.ImportexportcloudConstants.STORAGE_PATH_SEPARATOR;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;


public abstract class AbstractCloudDataObjectPopulator
{
	private static final String DATE_PATTERN = "MM-dd-yyyy HH:mm:ss";

	protected String resolveTitle(final String key)
	{
		final String splittedPath = StringUtils.removeEnd(key, STORAGE_PATH_SEPARATOR);
		return splittedPath.contains(STORAGE_PATH_SEPARATOR)
				? StringUtils.substringAfterLast(splittedPath, STORAGE_PATH_SEPARATOR)
				: splittedPath;
	}

	protected String formatDate(final Date date)
	{
		return new SimpleDateFormat(DATE_PATTERN).format(date);
	}

	protected String getPathToDisplay(final String path)
	{
		return STORAGE_PATH_SEPARATOR + StringUtils.removeEnd(path, STORAGE_PATH_SEPARATOR);
	}
}
