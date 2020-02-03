package com.aimprosoft.importexportcloud.converters.populators;

import static com.aimprosoft.importexportcloud.constants.ImportexportcloudConstants.ROOT_FOLDER;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import org.apache.commons.lang.StringUtils;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.model.AWSs3StorageConfigModel;


public class AWSs3StorageConfigPopulator implements Populator<AWSs3StorageConfigModel, StorageConfigData>
{
	@Override
	public void populate(final AWSs3StorageConfigModel source, final StorageConfigData target) throws ConversionException
	{
		if (source != null)
		{
			target.setRegion(source.getRegion());
			final String bucketName = source.getBucketName();
			target.setBucketName(bucketName);

			if (StringUtils.isNotBlank(bucketName))
			{
				target.setRootFolder(ROOT_FOLDER);
			}
		}
	}
}
