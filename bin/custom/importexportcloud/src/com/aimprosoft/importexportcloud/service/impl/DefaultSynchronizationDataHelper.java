package com.aimprosoft.importexportcloud.service.impl;

import com.aimprosoft.importexportcloud.enums.TaskInfoScope;
import com.aimprosoft.importexportcloud.exceptions.ImportException;
import com.aimprosoft.importexportcloud.exceptions.TaskException;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.TaskInfoModel;
import com.aimprosoft.importexportcloud.service.IemCMSSiteService;
import com.aimprosoft.importexportcloud.service.IemCatalogVersionService;
import com.aimprosoft.importexportcloud.service.SynchronizationDataHelper;
import com.aimprosoft.importexportcloud.service.TaskInfoService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.catalog.model.SyncItemJobModel;
import de.hybris.platform.catalog.model.classification.ClassificationSystemVersionModel;
import de.hybris.platform.catalog.synchronization.CatalogSynchronizationService;
import de.hybris.platform.catalog.synchronization.SyncConfig;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.cronjob.enums.ErrorMode;
import de.hybris.platform.cronjob.enums.JobLogLevel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class DefaultSynchronizationDataHelper implements SynchronizationDataHelper
{
	private static final Logger LOGGER = Logger.getLogger(DefaultSynchronizationDataHelper.class);

	private static final String QUALIFIER_SYNC_JOB_TEMPLATE = "sync %s:Staged->Online";

	private IemCMSSiteService iemCmsSiteService;

	private TaskInfoService<TaskInfoModel> taskInfoService;

	private CatalogSynchronizationService catalogSynchronizationService;

	private IemCatalogVersionService iemCatalogVersionService;

	private UserService userService;

	private ModelService modelService;

	@Override
	public void synchronizeData(final TaskInfoData taskInfoData) throws ImportException, TaskException
	{
		final TaskInfoData taskExportInfo = taskInfoService.getTaskInfoFromJSON(taskInfoData.getDownloadedFilePath());
		final String taskScopeCode = taskExportInfo.getTaskInfoScopeCode();

		if (TaskInfoScope.SITESCOPE.getCode().equals(taskScopeCode))
		{
			final String cmsSiteUid = taskExportInfo.getCmsSiteUid();
			final CMSSiteModel cmsSite = iemCmsSiteService.getCMSSiteForUid(cmsSiteUid);

			final Collection<CatalogVersionModel> allCatalogVersions = iemCatalogVersionService
					.getExportableCatalogVersionsForCmsSite(cmsSite);

			for (final CatalogVersionModel allCatalogVersion : allCatalogVersions)
			{
				processSynchronizationForCatalog(allCatalogVersion);
			}

		}
		else if (TaskInfoScope.CATALOGSCOPE.getCode().equals(taskScopeCode))
		{
			final String catalogNameAndVersion = taskExportInfo.getCatalogIdAndVersionName();
			final String catalogId = StringUtils.substringBefore(catalogNameAndVersion, ":");
			final String catalogVersionName = StringUtils.substringAfter(catalogNameAndVersion, ":");

			final CatalogVersionModel sourceCatalogVersion = iemCatalogVersionService
					.getCatalogVersionModelWithoutSearchRestrictions(catalogId, catalogVersionName);

			processSynchronizationForCatalog(sourceCatalogVersion);

		}
		else
		{
			LOGGER.warn("Couldn't detect the scope of synchronize");
		}
	}

	private void processSynchronizationForCatalog(final CatalogVersionModel sourceCatalogVersion) throws ImportException
	{
		final String catalogId = sourceCatalogVersion.getCatalog().getId();

		final String qualifierSyncJob = String.format(QUALIFIER_SYNC_JOB_TEMPLATE, catalogId);

		if (!(sourceCatalogVersion instanceof ClassificationSystemVersionModel))
		{
			final CatalogVersionModel targetCatalogVersion = sourceCatalogVersion.getCatalog().getActiveCatalogVersion();

/*			This approach is used to add read permissions for catalogs to "CBMUser".
			Because "CBMUser" may not have read permission. Due of this we cannot manually synchronize catalogs
*/
			addReadPrincipalForCatalogs(Arrays.asList(sourceCatalogVersion, targetCatalogVersion));

			final SyncItemJobModel syncItemJobModel = catalogSynchronizationService
					.getSyncJob(sourceCatalogVersion, targetCatalogVersion, qualifierSyncJob);

			if (syncItemJobModel != null)
			{
				final SyncConfig syncConfig = createSyncConfig(syncItemJobModel);

				LOGGER.info(" Synchronization is starting for " + catalogId);

				catalogSynchronizationService.synchronize(syncItemJobModel, syncConfig);

			}
			else
			{
				throw new ImportException("Can not find SyncItemJobModel by qualifier \"" + qualifierSyncJob + "\"");
			}
		}
		else
		{
			LOGGER.warn("Synchronization cannot be performed for " + catalogId);
		}
	}

	private void addReadPrincipalForCatalogs(final List<CatalogVersionModel> catalogVersions)
	{
		final UserModel currentUser = userService.getCurrentUser();

		if (!userService.isAdmin(currentUser))
		{
			boolean needToSave = false;

			for (final CatalogVersionModel catalogVersion : catalogVersions)
			{
				List<PrincipalModel> readPrincipals = catalogVersion.getReadPrincipals();
				if (readPrincipals == null)
				{
					final List<PrincipalModel> newReadPrincipals = new ArrayList<>();
					newReadPrincipals.add(currentUser);
					catalogVersion.setReadPrincipals(newReadPrincipals);
					needToSave = true;
				}
				else if (!readPrincipals.contains(currentUser))
				{
					readPrincipals = new ArrayList<>(readPrincipals);
					readPrincipals.add(currentUser);
					catalogVersion.setReadPrincipals(readPrincipals);
					needToSave = true;
				}
			}
			if (needToSave)
			{
				modelService.saveAll(catalogVersions);
			}
		}
	}

	private SyncConfig createSyncConfig(final SyncItemJobModel syncJob)
	{
		final SyncConfig syncConfig = new SyncConfig();
		syncConfig.setCreateSavedValues(Boolean.FALSE);
		syncConfig.setForceUpdate(Boolean.TRUE);
		syncConfig.setSynchronous(Boolean.FALSE);
		syncConfig.setAbortWhenCollidingSyncIsRunning(Boolean.TRUE);
		syncConfig.setLogToFile(syncJob.getLogToFile());
		syncConfig.setLogToDatabase(syncJob.getLogToDatabase());
		syncConfig.setKeepCronJob(syncJob.getRemoveOnExit());
		syncConfig.setLogLevelDatabase(JobLogLevel.WARNING);
		syncConfig.setLogLevelFile(JobLogLevel.INFO);
		syncConfig.setErrorMode(ErrorMode.FAIL);
		return syncConfig;
	}

	public IemCMSSiteService getIemCmsSiteService()
	{
		return iemCmsSiteService;
	}

	@Required
	public void setIemCmsSiteService(final IemCMSSiteService iemCmsSiteService)
	{
		this.iemCmsSiteService = iemCmsSiteService;
	}

	public TaskInfoService<TaskInfoModel> getTaskInfoService()
	{
		return taskInfoService;
	}

	@Required
	public void setTaskInfoService(final TaskInfoService<TaskInfoModel> taskInfoService)
	{
		this.taskInfoService = taskInfoService;
	}

	public CatalogSynchronizationService getCatalogSynchronizationService()
	{
		return catalogSynchronizationService;
	}

	@Required
	public void setCatalogSynchronizationService(final CatalogSynchronizationService catalogSynchronizationService)
	{
		this.catalogSynchronizationService = catalogSynchronizationService;
	}

	public IemCatalogVersionService getIemCatalogVersionService()
	{
		return iemCatalogVersionService;
	}

	@Required
	public void setIemCatalogVersionService(final IemCatalogVersionService iemCatalogVersionService)
	{
		this.iemCatalogVersionService = iemCatalogVersionService;
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
