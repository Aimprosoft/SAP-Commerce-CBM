package com.aimprosoft.importexportcloud.converters.populators;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.ImportTaskInfoModel;


public class ImportTaskInfoReversePopulator implements Populator<TaskInfoData, ImportTaskInfoModel>
{
	@Override
	public void populate(final TaskInfoData taskInfoData, final ImportTaskInfoModel importTaskInfoModel) throws ConversionException
	{
		if (taskInfoData != null)
		{
			importTaskInfoModel.setExternalPath(taskInfoData.getCloudFileDownloadPathToDisplay());
		}
	}
}
