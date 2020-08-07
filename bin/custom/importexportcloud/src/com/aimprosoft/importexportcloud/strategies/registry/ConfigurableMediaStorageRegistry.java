package com.aimprosoft.importexportcloud.strategies.registry;

import com.aimprosoft.importexportcloud.model.LocalStorageConfigModel;
import com.aimprosoft.importexportcloud.model.StorageConfigModel;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.media.storage.MediaStorageConfigService;
import de.hybris.platform.media.storage.MediaStorageStrategy;
import de.hybris.platform.media.storage.impl.DefaultMediaStorageRegistry;
import de.hybris.platform.media.url.MediaURLStrategy;
import de.hybris.platform.servicelayer.media.MediaService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;


public class ConfigurableMediaStorageRegistry extends DefaultMediaStorageRegistry
{
	@Autowired
	private MediaStorageStrategy configurableMediaStorageStrategy;
	@Autowired
	private MediaURLStrategy configurableMediaUrlStrategy;
	@Autowired
	private MediaService mediaService;

	@Override
	public MediaStorageStrategy getStorageStrategyForFolder(final MediaStorageConfigService.MediaFolderConfig config)
	{
		return isOOTBStrategyUsed(config) ? super.getStorageStrategyForFolder(config) : configurableMediaStorageStrategy;
	}

	@Override
	public MediaURLStrategy getURLStrategyForFolder(final MediaStorageConfigService.MediaFolderConfig mediaFolderConfig,
			final Collection<String> collection)
	{
		return isConfigurableStrategyAvailableForUrl(mediaFolderConfig)
				? configurableMediaUrlStrategy
				: super.getURLStrategyForFolder(mediaFolderConfig, collection);
	}

	private boolean isOOTBStrategyUsed(final MediaStorageConfigService.MediaFolderConfig config)
	{
		final MediaFolderModel mediaFolder = mediaService.getFolder(config.getFolderQualifier());
		final StorageConfigModel currentStorageConfig = mediaFolder.getCurrentStorageConfig();
		final StorageConfigModel targetStorageConfig = mediaFolder.getTargetStorageConfig();

		return targetStorageConfig == null && currentStorageConfig == null ||
				targetStorageConfig == null && currentStorageConfig instanceof LocalStorageConfigModel;
	}

	private boolean isConfigurableStrategyAvailableForUrl(final MediaStorageConfigService.MediaFolderConfig config)
	{
		final MediaFolderModel mediaFolder = mediaService.getFolder(config.getFolderQualifier());

		final StorageConfigModel currentStorageConfig = mediaFolder.getCurrentStorageConfig();

		return currentStorageConfig != null && !(currentStorageConfig instanceof LocalStorageConfigModel);

	}
}
