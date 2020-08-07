package com.aimprosoft.importexportcloud.service.connection.impl;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.media.MediaSource;
import de.hybris.platform.servicelayer.internal.service.AbstractBusinessService;

import de.hybris.platform.util.Config;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.service.StorageConfigService;
import com.aimprosoft.importexportcloud.service.connection.ConnectionService;
import com.aimprosoft.importexportcloud.service.validators.StorageConfigValidator;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;


public abstract class AbstractConnectionService extends AbstractBusinessService implements ConnectionService
{
	private static final int MAX_SIGNED_URL_DURATION = 7;

	private StorageConfigService storageConfigService;

	private StorageConfigValidator storageConfigValidator;

	public StorageConfigService getStorageConfigService()
	{
		return storageConfigService;
	}

	@Required
	public void setStorageConfigService(final StorageConfigService storageConfigService)
	{
		this.storageConfigService = storageConfigService;
	}

	public StorageConfigValidator getStorageConfigValidator()
	{
		return storageConfigValidator;
	}

	@Required
	public void setStorageConfigValidator(final StorageConfigValidator storageConfigValidator)
	{
		this.storageConfigValidator = storageConfigValidator;
	}

	protected Duration getSignedUrlDuration()
	{
		return Duration.ofDays(Config
				.getInt("migration.signed.url.duration.days", MAX_SIGNED_URL_DURATION));
	}

	public String getUrlForMedia(final MediaSource mediaSource, final StorageConfigData storageConfig, final String location)
			throws CloudStorageException
	{
		final boolean enableSavingUrls = storageConfig.getEnableSavingUrls();
		String mediaUrl;
		if (enableSavingUrls)
		{
			mediaUrl = processWithSavingMedia(mediaSource, storageConfig, location);
		}
		else
		{
			mediaUrl = obtainPublicURL(storageConfig, location);
		}

		return mediaUrl;
	}

	private String processWithSavingMedia(final MediaSource mediaSource, final StorageConfigData storageConfig,
			final String location)
			throws CloudStorageException
	{
		final boolean useSignedUrl = storageConfig.isUseSignedUrls();
		MediaModel media = getModelService().get(PK.fromLong(mediaSource.getMediaPk()));

		final String url;
		if (useSignedUrl)
		{
			url = processWithUsingSingedUrl(media, storageConfig, location);
		}
		else
		{
			final String oldMediaUrl = media.getStorageURL();
			if (oldMediaUrl == null)
			{
				url = getSavedMediaUrl(storageConfig, location, media);
			}
			else
			{
				url = oldMediaUrl;
			}
		}
		return url;
	}

	private String processWithUsingSingedUrl(final MediaModel media,
			final StorageConfigData storageConfig, final String location) throws CloudStorageException
	{
		Date expires = getDateOfExpiry(storageConfig);

		final Date expiredDateForStorageURL = media.getExpiredDateForStorageURL();
		if (expiredDateForStorageURL == null || expiredDateForStorageURL.compareTo(new Date()) < 0)
		{
			media.setExpiredDateForStorageURL(expires);
			return getSavedMediaUrl(storageConfig, location, media);
		}
		else
		{
			return media.getStorageURL();
		}
	}

	private String getSavedMediaUrl(final StorageConfigData storageConfig, final String location, final MediaModel media)
			throws CloudStorageException
	{
		final String mediaUrl = obtainPublicURL(storageConfig, location);
		media.setStorageURL(mediaUrl);
		modelService.save(media);
		return mediaUrl;
	}

	protected Date getDateOfExpiry(StorageConfigData storageConfig){
		final boolean useSignedUrl = storageConfig.isUseSignedUrls();
		final Duration signedUrlDuration = getSignedUrlDuration();

		return BooleanUtils.isFalse(useSignedUrl) ? null : Date.from(Instant.now().plus(signedUrlDuration));
	}
}
