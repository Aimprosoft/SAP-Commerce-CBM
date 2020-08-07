package com.aimprosoft.importexportcloud.facades.impl;

import com.aimprosoft.importexportcloud.facades.MediaFolderFacade;
import com.aimprosoft.importexportcloud.facades.data.MediaFolderData;
import com.aimprosoft.importexportcloud.service.MediaFolderService;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;


public class DefaultMediaFolderFacade implements MediaFolderFacade
{

	private Converter<MediaFolderModel, MediaFolderData> mediaFolderConverter;

	private MediaFolderService mediaFolderService;

	@Override
	public List<MediaFolderData> getAllMediaFolders()
	{
		final List<MediaFolderModel> mediaFolders = mediaFolderService.getAllMediaFolders();
		return mediaFolderConverter.convertAll(mediaFolders);
	}

	public Converter<MediaFolderModel, MediaFolderData> getMediaFolderConverter()
	{
		return mediaFolderConverter;
	}

	@Required
	public void setMediaFolderConverter(
			final Converter<MediaFolderModel, MediaFolderData> mediaFolderConverter)
	{
		this.mediaFolderConverter = mediaFolderConverter;
	}

	public MediaFolderService getMediaFolderService()
	{
		return mediaFolderService;
	}

	@Required
	public void setMediaFolderService(final MediaFolderService mediaFolderService)
	{
		this.mediaFolderService = mediaFolderService;
	}
}
