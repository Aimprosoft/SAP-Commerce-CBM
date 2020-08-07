package com.aimprosoft.importexportcloud.strategies.url;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.service.connection.ConnectionService;
import com.aimprosoft.importexportcloud.strategies.IemServiceTypeLocator;
import de.hybris.platform.media.MediaSource;
import de.hybris.platform.media.storage.MediaStorageConfigService;
import de.hybris.platform.media.storage.MediaStorageRegistry;
import de.hybris.platform.media.url.MediaURLStrategy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Objects;


public class ConfigurableMediaURLStrategy implements MediaURLStrategy
{
	private static final Logger LOG = LoggerFactory.getLogger(ConfigurableMediaURLStrategy.class);

	@Resource
	private StorageConfigFacade storageConfigFacade;

	@Resource
	private IemServiceTypeLocator iemServiceTypeLocator;

	@Resource(name = "defaultMediaStorageRegistry")
	private MediaStorageRegistry mediaStorageRegistry;

	@Override
	public String getUrlForMedia(final MediaStorageConfigService.MediaFolderConfig mediaFolderConfig,
			final MediaSource mediaSource)
	{
		final String location = mediaSource.getLocation();
		final StorageConfigData storageConfig = obtainStorageConfigFromLocation(location);

		final ConnectionService connectionService = iemServiceTypeLocator.getConnectionService(storageConfig);

		if (connectionService == null)
		{
			final MediaURLStrategy defaultUrlStrategy = mediaStorageRegistry
					.getURLStrategyForFolder(mediaFolderConfig, Collections.emptyList());

			return defaultUrlStrategy.getUrlForMedia(mediaFolderConfig, mediaSource);
		}

		try
		{
			Objects.requireNonNull(storageConfig);
			return connectionService.getUrlForMedia(mediaSource, storageConfig, location);
		}
		catch (CloudStorageException e)
		{
			LOG.warn("Could not get URL for media DataPk={}, storage type={}", mediaSource.getDataPk(),
					storageConfig.getStorageTypeData().getCode());
		}

		return null;
	}

	private StorageConfigData obtainStorageConfigFromLocation(final String location)
	{
		final String storageCode = StringUtils.substringBetween(location, "storageCode=", "/");

		if (storageCode == null)
		{
			return null;
		}

		return storageConfigFacade.getStorageConfigData(storageCode);
	}
}
