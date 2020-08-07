package com.aimprosoft.importexportcloud.strategies.migration.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.cronjob.model.MediaFolderStructureMigrationCronJobModel;
import de.hybris.platform.servicelayer.exceptions.SystemException;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.tx.Transaction;
import de.hybris.platform.tx.TransactionBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.model.MigrationTaskInfoModel;
import com.aimprosoft.importexportcloud.model.StorageConfigModel;
import com.aimprosoft.importexportcloud.strategies.migration.IemMediaMigrationStrategy;
import com.google.common.base.Preconditions;

import reactor.util.StringUtils;


public class ConfigurableMediaMigrationStrategy implements IemMediaMigrationStrategy
{
	private static final Logger LOG = LoggerFactory.getLogger(ConfigurableMediaMigrationStrategy.class);

	public static final String CATALOG_VERSION = "Staged";

	private MediaService mediaService;

	private ModelService modelService;

	@Override
	public FlexibleSearchQuery createFetchQuery(final MediaFolderStructureMigrationCronJobModel cjm)
	{
		Preconditions.checkArgument(cjm != null, "MediaFolderStructureMigrationCronJobModel is required!");

		final MediaFolderModel mediaFolder = cjm.getMediaFolder();

		if (mediaFolder == null)
		{
			throw new SystemException("CronJob does not contain folder to migrate.");
		}
		else
		{
			final Map<String, Object> params = new HashMap<>();
			params.put(CatalogVersionModel.VERSION, CATALOG_VERSION);
			params.put(MediaModel.FOLDER, mediaFolder);

			final String fetchQuery = "SELECT {" + MediaModel.PK + "} "
					+ "FROM {" + MediaModel._TYPECODE + " AS m "
					+ "JOIN " + CatalogVersionModel._TYPECODE
					+ " AS cv ON {cv:" + CatalogVersionModel.PK + "} = {m." + MediaModel.CATALOGVERSION
					+ "}} "
					+ "WHERE {" + MediaModel.FOLDER + "}=?folder "
					+ "AND {cv:" + CatalogVersionModel.VERSION + "}=?version";
			return new FlexibleSearchQuery(fetchQuery, params);
		}
	}

	@Override
	public void process(final List<MediaModel> elements)
	{
		if (CollectionUtils.isNotEmpty(elements))
		{
			final MediaFolderModel folder = elements.get(0).getFolder();
			validateParameterNotNull(folder, "Media folder is required for migration");
			final MigrationTaskInfoModel migrationTask = folder.getLastMigrationTaskInfo();
			validateParameterNotNull(migrationTask, "No migration task info provided");
			final StorageConfigModel targetStorage = migrationTask.getStorageConfig();
			validateParameterNotNull(targetStorage, "Target storage config is required");

			int migratedCount = 0;
			final Set<MediaModel> migratedMedias = new HashSet<>();

			for (final MediaModel media : elements)
			{
				try
				{
					if (isMediaAvailableForMigration(media, targetStorage))
					{
//						Condition added to avoid generation and encoding of long names of cache files while migration process.
//						File names with name length more than 255 can invoke exceptions with creation files and DB errors.
						if (StringUtils.isEmpty(media.getRealFileName()))
						{
							media.setRealFileName(media.getCode());
						}
//						Transaction is way to prevent removing media when it is already removed during migration but
//						Cloud service or Network is unavailable and media is lost.
//						With using transaction operations is roll backed and media is not removed
						Transaction.current().execute(new TransactionBody()
						{
							@Override
							public Boolean execute() throws Exception
							{
								relocateMediaData(media, targetStorage);
								return true;
							}
						});

						migratedCount++;
						media.setActualStorage(targetStorage);
						media.setStorageURL(null);
						media.setExpiredDateForStorageURL(null);
						migratedMedias.add(media);
					}
				}
				catch (Exception e)
				{
					LOG.error("Media code={}, location={} cannot be written to {}", media.getCode(),
							media.getLocation(), targetStorage.getCode(), e);
					break;
				}
			}
			LOG.info("Medias were migrated on storage " + targetStorage.getType().getCode() + ". Storage code:" + targetStorage
					.getCode());

			setTotalCountMigrated(migrationTask, migratedCount);

			modelService.saveAll(migratedMedias);

		}
	}

	private void relocateMediaData(final MediaModel media, final StorageConfigModel targetStorage) throws IOException
	{

		try (final InputStream inputStream = mediaService.getStreamFromMedia(media))
		{
			mediaService.removeDataFromMediaQuietly(media);

			mediaService.setStreamForMedia(media, inputStream, media.getRealFileName(), media.getMime(), media.getFolder());

			LOG.debug("Successfully migrated media code={}, location={} to the actual storage={}", media.getCode(),
					media.getLocation(), targetStorage.getCode());
		}

	}

	private boolean isMediaAvailableForMigration(final MediaModel media, final StorageConfigModel targetStorage)
	{

		if (media.getActualStorage() != null)
		{
			final String actualStorageCode = media.getActualStorage().getCode();
			final String targetStorageCode = targetStorage.getCode();

			if (targetStorageCode.equals(actualStorageCode))
			{
				LOG.info("Media code={}, location={} has been already relocated from {} to {}", media.getCode(),
						media.getLocation(), actualStorageCode, targetStorageCode);

				return false;
			}
		}

		if (!mediaService.hasData(media))
		{
			LOG.warn("Media code={}, location={} was not stored in system, skipping", media.getCode(),
					media.getLocation());

			return false;
		}
		return true;
	}

	private void setTotalCountMigrated(final MigrationTaskInfoModel migrationTask, final int migratedCount)
	{

		final int totalMediasMigrated = migrationTask.getTotalCountMigratedMedia() + migratedCount;

		migrationTask.setTotalCountMigratedMedia(totalMediasMigrated);

		modelService.save(migrationTask);

	}

	public MediaService getMediaService()
	{
		return mediaService;
	}

	@Required
	public void setMediaService(final MediaService mediaService)
	{
		this.mediaService = mediaService;
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}
}
