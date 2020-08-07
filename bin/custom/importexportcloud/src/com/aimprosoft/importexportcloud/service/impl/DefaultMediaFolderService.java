package com.aimprosoft.importexportcloud.service.impl;

import com.aimprosoft.importexportcloud.service.MediaFolderService;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;


public class DefaultMediaFolderService implements MediaFolderService
{
	private GenericDao<MediaFolderModel> mediaFolderGenericDao;

	@Override
	public List<MediaFolderModel> getAllMediaFolders()
	{
		return mediaFolderGenericDao.find();
	}

	public GenericDao<MediaFolderModel> getMediaFolderGenericDao()
	{
		return mediaFolderGenericDao;
	}

	@Required
	public void setMediaFolderGenericDao(final GenericDao<MediaFolderModel> mediaFolderGenericDao)
	{
		this.mediaFolderGenericDao = mediaFolderGenericDao;
	}
}
