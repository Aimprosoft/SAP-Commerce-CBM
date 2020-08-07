package com.aimprosoft.importexportcloud.export;

import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;


public interface ExportScriptGeneratorService
{
	/**
	 * Generated export script.
	 *
	 * @param exportTaskInfo export task info model for current exporting process
	 * @return export script
	 */
	String generateExportScript(ExportTaskInfoModel exportTaskInfo);
}
