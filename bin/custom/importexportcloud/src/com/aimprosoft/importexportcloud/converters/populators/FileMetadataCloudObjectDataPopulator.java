package com.aimprosoft.importexportcloud.converters.populators;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.util.Date;

import com.aimprosoft.importexportcloud.facades.data.CloudObjectData;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;


public class FileMetadataCloudObjectDataPopulator extends AbstractCloudDataObjectPopulator
		implements Populator<Metadata, CloudObjectData>
{
	@Override
	public void populate(final Metadata metadata, final CloudObjectData cloudObjectData) throws ConversionException
	{
		if (metadata != null)
		{
			final String pathDisplay = metadata.getPathDisplay();
			cloudObjectData.setTitle(resolveTitle(pathDisplay));
			cloudObjectData.setPathDisplay(pathDisplay);

			populateAdditionalInfo(metadata, cloudObjectData);
		}
	}

	protected void populateAdditionalInfo(final Metadata metadata, final CloudObjectData cloudObjectData)
	{
		cloudObjectData.setFolder(Boolean.FALSE);
		if (metadata instanceof FileMetadata)
		{
			cloudObjectData.setName(((FileMetadata) metadata).getId());
			final Date clientModified = ((FileMetadata) metadata).getClientModified();
			cloudObjectData.setModifiedDate(formatDate(clientModified));
		}
	}
}
