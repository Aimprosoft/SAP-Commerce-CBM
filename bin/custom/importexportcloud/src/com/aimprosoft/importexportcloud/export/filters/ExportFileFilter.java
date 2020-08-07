package com.aimprosoft.importexportcloud.export.filters;

import java.nio.file.Path;

import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;


public interface ExportFileFilter
{
	/**
	 * Determines whether the filter is applicable for current export conditions.
	 *
	 * @param exportTaskInfoModel export task info model
	 * @return <code>true</code> if the filter is applicable
	 */
	default boolean isApplicable(final ExportTaskInfoModel exportTaskInfoModel)
	{
		return Boolean.TRUE;
	}

	/**
	 * Modifies some specific files in <code>exportImpexAndCSVsFilePath</code>.
	 *
	 * @param exportTaskInfoModel        export task info model
	 * @param exportImpexAndCSVsFilePath zip file that contains import script and data csv files
	 * @throws ExportException if an error occurs during modifying process
	 */
	void filter(ExportTaskInfoModel exportTaskInfoModel, Path exportImpexAndCSVsFilePath) throws ExportException;
}
