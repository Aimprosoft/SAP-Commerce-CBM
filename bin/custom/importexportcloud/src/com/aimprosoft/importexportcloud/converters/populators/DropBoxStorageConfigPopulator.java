package com.aimprosoft.importexportcloud.converters.populators;

import static com.aimprosoft.importexportcloud.constants.ImportexportcloudConstants.ROOT_FOLDER;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.model.DropBoxStorageConfigModel;


public class DropBoxStorageConfigPopulator implements Populator<DropBoxStorageConfigModel, StorageConfigData>
{
	@Override
	public void populate(final DropBoxStorageConfigModel source, final StorageConfigData target) throws ConversionException
	{
		if (source != null)
		{
			target.setAccessToken(source.getAccessToken());
			target.setRootFolder(ROOT_FOLDER);
		}
	}
}
