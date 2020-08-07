package com.aimprosoft.importexportcloud.facades.impl;

import com.aimprosoft.importexportcloud.enums.TaskInfoStatus;
import com.aimprosoft.importexportcloud.exceptions.MigrationException;
import com.aimprosoft.importexportcloud.facades.MigrationFacade;
import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.MigrationTaskInfoModel;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.cronjob.model.MediaFolderStructureMigrationCronJobModel;
import de.hybris.platform.servicelayer.cronjob.CronJobService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


public class DefaultMigrationFacade implements MigrationFacade
{
	private CronJobService cronJobService;

	private KeyGenerator keyGenerator;

	private MediaService mediaService;

	private ModelService modelService;

	private StorageConfigFacade storageConfigFacade;

	private UserService userService;

	private Converter<TaskInfoData, MigrationTaskInfoModel> migrationTaskInfoReverseConverter;

	@Override
	public TaskInfoData runMigrationCronJob(final TaskInfoData taskInfoData) throws MigrationException
	{
		validateParameterNotNullStandardMessage("taskInfoData", taskInfoData);
		validateParameterNotNullStandardMessage("folderQualifier", taskInfoData.getMediaFolderQualifier());
		validateParameterNotNullStandardMessage("targetStorageConfig", taskInfoData.getConfig());

		final MediaFolderModel mediaFolder = mediaService.getFolder(taskInfoData.getMediaFolderQualifier());

		final boolean isBlockedForMigration = mediaFolder.isBlockedForMigration();

		if (BooleanUtils.isTrue(isBlockedForMigration))
		{
			throw new MigrationException(
					"You can't migrate folder with qualifier " + mediaFolder.getQualifier() + " because the process is running");
		}

		final MigrationTaskInfoModel migrationTaskInfo = getMigrationTaskInfo(taskInfoData, mediaFolder);

		storageConfigFacade.setStorageConfigForFolder(mediaFolder, taskInfoData.getConfig());

		//start migration cron job
		final MediaFolderStructureMigrationCronJobModel migrationCronJob = modelService
				.create(MediaFolderStructureMigrationCronJobModel.class);
		migrationCronJob.setMediaFolder(mediaFolder);
		migrationCronJob.setJob(cronJobService.getJob("configurableMediaMigrationJob"));

		migrationTaskInfo.setCronJob(migrationCronJob);

		modelService.saveAll(mediaFolder, migrationTaskInfo, migrationCronJob);

		cronJobService.performCronJob(migrationCronJob);

		return taskInfoData;
	}


	private MigrationTaskInfoModel getMigrationTaskInfo(final TaskInfoData taskInfoData, final MediaFolderModel mediaFolder)
	{
		MigrationTaskInfoModel migrationTaskInfo;

		if (mediaFolder.getLastMigrationTaskInfo() != null)
		{
			migrationTaskInfo = mediaFolder.getLastMigrationTaskInfo();

			if (migrationTaskInfo.getStatus().equals(TaskInfoStatus.PARTLYMIGRATED)
					|| migrationTaskInfo.getStatus().equals(TaskInfoStatus.ABORTED))
			{
				return migrationTaskInfo;
			}
			else
			{
				migrationTaskInfo = createMigrationTaskInfo(taskInfoData, mediaFolder);
			}
		}
		else
		{
			migrationTaskInfo = createMigrationTaskInfo(taskInfoData, mediaFolder);
		}

		return migrationTaskInfo;

	}

	public MigrationTaskInfoModel createMigrationTaskInfo(final TaskInfoData taskInfoData, final MediaFolderModel mediaFolder)
	{

		final MigrationTaskInfoModel migrationTaskInfo = modelService.create(MigrationTaskInfoModel.class);
		migrationTaskInfoReverseConverter.convert(taskInfoData, migrationTaskInfo);
		mediaFolder.setLastMigrationTaskInfo(migrationTaskInfo);
		migrationTaskInfo.setStatus(TaskInfoStatus.STARTED);

		modelService.save(migrationTaskInfo);

		return migrationTaskInfo;
	}

	public CronJobService getCronJobService()
	{
		return cronJobService;
	}

	@Required
	public void setCronJobService(final CronJobService cronJobService)
	{
		this.cronJobService = cronJobService;
	}

	public KeyGenerator getKeyGenerator()
	{
		return keyGenerator;
	}

	@Required
	public void setKeyGenerator(final KeyGenerator keyGenerator)
	{
		this.keyGenerator = keyGenerator;
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

	public StorageConfigFacade getStorageConfigFacade()
	{
		return storageConfigFacade;
	}

	@Required
	public void setStorageConfigFacade(final StorageConfigFacade storageConfigFacade)
	{
		this.storageConfigFacade = storageConfigFacade;
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

	public Converter<TaskInfoData, MigrationTaskInfoModel> getMigrationTaskInfoReverseConverter()
	{
		return migrationTaskInfoReverseConverter;
	}

	@Required
	public void setMigrationTaskInfoReverseConverter(
			final Converter<TaskInfoData, MigrationTaskInfoModel> migrationTaskInfoReverseConverter)
	{
		this.migrationTaskInfoReverseConverter = migrationTaskInfoReverseConverter;
	}
}
