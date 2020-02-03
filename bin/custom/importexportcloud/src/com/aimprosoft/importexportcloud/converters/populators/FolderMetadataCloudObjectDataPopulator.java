package com.aimprosoft.importexportcloud.converters.populators;

import com.aimprosoft.importexportcloud.facades.data.CloudObjectData;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.Metadata;


public class FolderMetadataCloudObjectDataPopulator extends FileMetadataCloudObjectDataPopulator
{
	@Override
	protected void populateAdditionalInfo(final Metadata metadata, final CloudObjectData cloudObjectData)
	{
		if (metadata != null)
		{
			cloudObjectData.setFolder(Boolean.TRUE);
			cloudObjectData.setName(((FolderMetadata) metadata).getId());
		}
	}
}
