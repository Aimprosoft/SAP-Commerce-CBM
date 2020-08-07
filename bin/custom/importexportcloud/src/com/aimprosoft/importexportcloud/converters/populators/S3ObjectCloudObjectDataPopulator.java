package com.aimprosoft.importexportcloud.converters.populators;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.util.Date;

import com.aimprosoft.importexportcloud.facades.data.CloudObjectData;

import software.amazon.awssdk.services.s3.model.S3Object;


public class S3ObjectCloudObjectDataPopulator extends AbstractCloudDataObjectPopulator
		implements Populator<S3Object, CloudObjectData>
{
	@Override
	public void populate(final S3Object s3Object, final CloudObjectData cloudObjectData) throws ConversionException
	{
		if (s3Object != null)
		{
			final String path = s3Object.key();
			cloudObjectData.setName(path);
			cloudObjectData.setTitle(resolveTitle(path));
			cloudObjectData.setPathDisplay(getPathToDisplay(path));
			cloudObjectData.setFolder(false);
			cloudObjectData.setModifiedDate(formatDate(Date.from(s3Object.lastModified())));
		}
	}
}
