package com.aimprosoft.importexportcloud.job;

import com.aimprosoft.importexportcloud.enums.TaskInfoStatus;
import com.aimprosoft.importexportcloud.event.MigrationTaskEvent;
import com.aimprosoft.importexportcloud.exceptions.AbortMigrationException;
import com.aimprosoft.importexportcloud.model.MigrationTaskInfoModel;
import com.aimprosoft.importexportcloud.model.StorageConfigModel;
import com.aimprosoft.importexportcloud.strategies.migration.IemMediaMigrationStrategy;
import de.hybris.platform.core.LazyLoadItemList;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.cronjob.model.MediaFolderStructureMigrationCronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.impl.LazyLoadModelList;
import de.hybris.platform.servicelayer.search.internal.resolver.ItemObjectResolver;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collections;
import java.util.Date;
import java.util.List;


public class ConfigurableMediaMigrationJob extends AbstractJobPerformable<CronJobModel>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurableMediaMigrationJob.class);

	public static final int MIN_VALUE_OF_PAGE_SIZE = 1;

	private int pageSize;

	private UserService userService;

	private EventService eventService;

	private ItemObjectResolver modelResolver;

	private IemMediaMigrationStrategy iemMediaMigrationStrategy;

	@Override
	public PerformResult perform(final CronJobModel cronJob)
	{
		boolean caughtExeption = false;

		final MediaFolderStructureMigrationCronJobModel mediaFolderStructureMigrationCronJob = (MediaFolderStructureMigrationCronJobModel) cronJob;

		final MediaFolderModel mediaFolder = mediaFolderStructureMigrationCronJob.getMediaFolder();

		final MigrationTaskInfoModel migrationTaskInfo = mediaFolder.getLastMigrationTaskInfo();

		final FlexibleSearchQuery query = iemMediaMigrationStrategy
				.createFetchQuery(mediaFolderStructureMigrationCronJob);
		try
		{
			if (query == null)
			{
				throw new IllegalStateException("The FlexibleSearchQuery object was null, cannot procceed!");
			}

			selectElementsAndCheckProcessMigration(query, mediaFolder, migrationTaskInfo,
					mediaFolderStructureMigrationCronJob);
		}
		catch (AbortMigrationException e)
		{
			LOGGER.info(e.getMessage() + " CronJob code: {}, task info code: {}", mediaFolderStructureMigrationCronJob.getCode(),
					migrationTaskInfo.getCode());
			publishEventAndUpdateMigrationStatus(TaskInfoStatus.ABORTED, mediaFolder, migrationTaskInfo);
			return new PerformResult(CronJobResult.UNKNOWN, CronJobStatus.ABORTED);
		}
		catch (Exception e)
		{
			caughtExeption = true;

			LOGGER.error("Caught exception during process call. " + e.getClass().getName() + ": " + e.getMessage());

			publishEventAndUpdateMigrationStatus(TaskInfoStatus.FAILED, mediaFolder, migrationTaskInfo);
		}

		return new PerformResult(caughtExeption ? CronJobResult.FAILURE : CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

	private void selectElementsAndCheckProcessMigration(final FlexibleSearchQuery query, final MediaFolderModel mediaFolder,
			final MigrationTaskInfoModel migrationTaskInfo, final CronJobModel cronJob) throws AbortMigrationException
	{
		final List<Class> expectedClassList = query.getResultClassList();

		query.setResultClassList(Collections.singletonList(PK.class));

		// Added to avoid restrictions for media under non-admin user
		final SearchResult<PK> searchRes = sessionService.executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public Object execute()
			{
				return flexibleSearchService.search(query);
			}
		}, userService.getAdminUser());

		final int totalCount = searchRes.getTotalCount();

		setTaskStarted(migrationTaskInfo, mediaFolder, totalCount);

		checkAndProcessMigration(totalCount, searchRes, expectedClassList, cronJob);

		final TaskInfoStatus status = getStatusAfterMigration(migrationTaskInfo, totalCount);

		publishEventAndUpdateMigrationStatus(status, mediaFolder, migrationTaskInfo);
	}

	private TaskInfoStatus getStatusAfterMigration(final MigrationTaskInfoModel migrationTaskInfo, final int totalCount)
	{
		final int countCountMigratedMedia = migrationTaskInfo.getTotalCountMigratedMedia();

		final TaskInfoStatus status = totalCount == countCountMigratedMedia ?
				TaskInfoStatus.COMPLETED :
				TaskInfoStatus.PARTLYMIGRATED;

		LOGGER.info("Finished media migration code={}, {} total elements, {} migrated, {} skipped",
				migrationTaskInfo.getCode(), totalCount,
				countCountMigratedMedia, totalCount - countCountMigratedMedia);

		return status;
	}

	private void setTaskStarted(final MigrationTaskInfoModel migrationTaskInfo, final MediaFolderModel mediaFolder,
			final int totalCount)
	{
		mediaFolder.setBlockedForMigration(Boolean.TRUE);
		migrationTaskInfo.setStatus(TaskInfoStatus.MIGRATING);

		modelService.saveAll(migrationTaskInfo, mediaFolder);

		LOGGER.info("Starting media migration code={}, {} elements are found", migrationTaskInfo.getCode(), totalCount);
	}

	private void publishEventAndUpdateMigrationStatus(final TaskInfoStatus status, final MediaFolderModel mediaFolder,
			final MigrationTaskInfoModel migrationTaskInfo)
	{
		migrationTaskInfo.setStatus(status);
		mediaFolder.setBlockedForMigration(Boolean.FALSE);
		migrationTaskInfo.setFinishedDate(new Date());

		if (TaskInfoStatus.COMPLETED.equals(status))
		{
			final StorageConfigModel currentStorage = migrationTaskInfo.getStorageConfig();
			mediaFolder.setCurrentStorageConfig(currentStorage);
			mediaFolder.setTargetStorageConfig(null);
		}

		modelService.saveAll(mediaFolder, migrationTaskInfo);
		eventService.publishEvent(new MigrationTaskEvent(migrationTaskInfo, userService.getCurrentUser()));
	}

	private void checkAndProcessMigration(final int totalCount, final SearchResult<PK> searchRes,
			final List<Class> expectedClassList,
			final CronJobModel cronJob)
			throws AbortMigrationException
	{
		for (int i = 0; i < totalCount; i += pageSize)
		{
			if (clearAbortRequestedIfNeeded(cronJob))
			{
				throw new AbortMigrationException("Migration has been aborted");
			}
			final List<PK> sublist = searchRes.getResult().subList(i, Math.min(i + pageSize, totalCount));

			final LazyLoadModelList<MediaModel> llml = new LazyLoadModelList<>(new LazyLoadItemList(null, sublist, pageSize),
					pageSize,
					expectedClassList, modelResolver);

			iemMediaMigrationStrategy.process(llml);

			for (final Object obj : llml)
			{
				modelService.detach(obj);
			}
		}
	}

	@Required
	public void setPageSize(final int pageSize)
	{
		if (pageSize < MIN_VALUE_OF_PAGE_SIZE)
		{
			throw new IllegalArgumentException("pageSize cannot be less one");
		}
		this.pageSize = pageSize;
	}

	@Override
	public boolean isAbortable()
	{
		return true;
	}

	public IemMediaMigrationStrategy getIemMediaMigrationStrategy()
	{
		return iemMediaMigrationStrategy;
	}

	@Required
	public void setIemMediaMigrationStrategy(final IemMediaMigrationStrategy iemMediaMigrationStrategy)
	{
		this.iemMediaMigrationStrategy = iemMediaMigrationStrategy;
	}

	public ItemObjectResolver getModelResolver()
	{
		return modelResolver;
	}

	@Required
	public void setModelResolver(final ItemObjectResolver modelResolver)
	{
		this.modelResolver = modelResolver;
	}

	public EventService getEventService()
	{
		return eventService;
	}

	@Required
	public void setEventService(final EventService eventService)
	{
		this.eventService = eventService;
	}

	public UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}
}
