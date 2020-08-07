package com.aimprosoft.importexportcloud.export.filters.impl;

import com.aimprosoft.importexportcloud.enums.TaskInfoScope;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;


public abstract class AbstractSiteRelatedExportFilter extends AbstractExportFileFilter
{
	@Override
	public boolean isApplicable(final ExportTaskInfoModel exportTaskInfoModel)
	{
		return exportTaskInfoModel.getTaskScope().equals(TaskInfoScope.SITESCOPE);
	}
}
