package com.aimprosoft.importexportcloud.export;

import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import de.hybris.platform.servicelayer.impex.ExportResult;


public interface ExportResultProcessor
{
	/**
	 * Creates and modifies one export zip file depending on whether export media is required or not.
	 * Sets final zip file path to task info data. Sets status to export task info model.
	 *
	 * @param taskInfoData        task info DTO
	 * @param exportTaskInfoModel export task info model for current exporting process
	 * @param exportResult        the result of exporting
	 * @throws ExportException if export result contains error or something went wrong
	 */
	void processExportResult(TaskInfoData taskInfoData, ExportTaskInfoModel exportTaskInfoModel, ExportResult exportResult)
			throws ExportException;
}
