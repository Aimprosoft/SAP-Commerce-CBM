package com.aimprosoft.importexportcloud.converters.populators;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import com.aimprosoft.importexportcloud.facades.data.CloudObjectData;

import software.amazon.awssdk.services.s3.model.CommonPrefix;


public class CommonPrefixCloudObjectDataPopulator extends AbstractCloudDataObjectPopulator
		implements Populator<CommonPrefix, CloudObjectData>
{
	@Override
	public void populate(final CommonPrefix commonPrefix, final CloudObjectData cloudObjectData) throws ConversionException
	{
		if (commonPrefix != null)
		{
			final String path = commonPrefix.prefix();
			cloudObjectData.setName(path);
			cloudObjectData.setTitle(resolveTitle(path));
			cloudObjectData.setPathDisplay(getPathToDisplay(path));
			cloudObjectData.setFolder(true);
		}
	}
}
