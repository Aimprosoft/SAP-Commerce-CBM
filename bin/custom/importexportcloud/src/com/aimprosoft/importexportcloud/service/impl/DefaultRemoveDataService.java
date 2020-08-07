package com.aimprosoft.importexportcloud.service.impl;

import de.hybris.platform.catalog.job.util.ImpexScriptGenerator;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.servicelayer.impex.ImpExResource;
import de.hybris.platform.servicelayer.impex.ImportConfig;
import de.hybris.platform.servicelayer.impex.ImportResult;
import de.hybris.platform.servicelayer.impex.ImportService;
import de.hybris.platform.servicelayer.impex.impl.StreamBasedImpExResource;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.CSVConstants;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.enums.TaskInfoScope;
import com.aimprosoft.importexportcloud.exceptions.TaskException;
import com.aimprosoft.importexportcloud.export.generators.ComposedTypesGenerator;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.TaskInfoModel;
import com.aimprosoft.importexportcloud.service.IemCMSSiteService;
import com.aimprosoft.importexportcloud.service.IemCatalogVersionService;
import com.aimprosoft.importexportcloud.service.RemoveDataService;
import com.aimprosoft.importexportcloud.service.TaskInfoService;


public class DefaultRemoveDataService implements RemoveDataService
{
	private static final Logger LOGGER = Logger.getLogger(DefaultRemoveDataService.class);

	private static final String REMOVE_CATEGORY = "remove Category";
	private static final String REMOVE_CATEGORY_DISABLE_INTERCEPTOR = "remove Category[disable.interceptor.beans='categoryRemovalValidator']";

	private ImpexScriptGenerator removeScriptGenerator;

	private UserService userService;

	private SessionService sessionService;

	private IemCMSSiteService iemCmsSiteService;

	private ImportService importService;

	private IemCatalogVersionService iemCatalogVersionService;

	private Map<Class, ComposedTypesGenerator> composedTypesGeneratorsMap;

	private ModelService modelService;

	private TaskInfoService<TaskInfoModel> taskInfoService;

	private Set<String> skipComposedTypeModels;

	@Override
	public void removeData(final TaskInfoData taskInfoData) throws TaskException
	{
		final StringBuilder removeScript = generateRemoveScriptByScope(taskInfoData);

		if (removeScript.length() < 1)
		{
			LOGGER.warn("There is no data to remove");

			return;
		}

		LOGGER.debug("Data for import: " + removeScript);

		final ImportConfig config = getImpexConfig(removeScript);
		final ImportResult result = importService.importData(config);

		LOGGER.info(" Import is running " + result.isRunning() + " finished " + result.isFinished() + " status "
				+ result.getCronJob().getStatus() + " cronjob " + result.getCronJob().getCode());
	}

	private StringBuilder generateRemoveScriptByScope(final TaskInfoData taskInfoData) throws TaskException
	{
		final TaskInfoData taskExportInfo = taskInfoService.getTaskInfoFromJSON(taskInfoData.getDownloadedFilePath());
		final String taskScopeCode = taskExportInfo.getTaskInfoScopeCode();
		final StringBuilder removeScript = new StringBuilder();

		if (TaskInfoScope.SITESCOPE.getCode().equals(taskScopeCode))
		{
			final String cmsSiteUid = taskExportInfo.getCmsSiteUid();
			final CMSSiteModel cmsSite = iemCmsSiteService.getCMSSiteForUid(cmsSiteUid);

			final Collection<CatalogVersionModel> allCatalogVersions = iemCatalogVersionService
					.getExportableCatalogVersionsForCmsSite(cmsSite);

			allCatalogVersions.forEach(catalogVersion -> removeScript.append(generateRemoveScript(catalogVersion)));
		}
		else if (TaskInfoScope.CATALOGSCOPE.getCode().equals(taskScopeCode))
		{
			final String catalogNameAndVersion = taskExportInfo.getCatalogIdAndVersionName();
			final String catalogId = StringUtils.substringBefore(catalogNameAndVersion, ":");
			final String catalogVersionName = StringUtils.substringAfter(catalogNameAndVersion, ":");

			final CatalogVersionModel catalogVersion = iemCatalogVersionService
					.getCatalogVersionModelWithoutSearchRestrictions(catalogId, catalogVersionName);

			removeScript.append(generateRemoveScript(catalogVersion));
		}
		else
		{
			LOGGER.warn("Couldn't detect the scope of export");
		}
		return removeScript;
	}

	private StringBuilder generateRemoveScript(final CatalogVersionModel catalogVersion)
	{
		final ComposedTypesGenerator composedTypesGenerator = composedTypesGeneratorsMap
				.get(catalogVersion.getCatalog().getClass());
		final Set<ComposedType> composedTypes = composedTypesGenerator.generateComposedTypes();

		final List<ComposedTypeModel> filteredComposedTypes = composedTypes.stream()
				.map(composedType -> modelService.<ComposedTypeModel>get(composedType.getPK()))
				.filter(model -> model.getCatalogVersionAttribute() != null)
				.filter(composedType -> !(skipComposedTypeModels != null && skipComposedTypeModels
						.contains(composedType.getCode())))
				.collect(Collectors.toList());

		return sessionService.executeInLocalView(
				new SessionExecutionBody()
				{
					@Override
					public StringBuilder execute()
					{
						LOGGER.info("Start generating data for catalog ID: [" + catalogVersion.getCatalog().getId() + "]"
								+ " with Version: [" + catalogVersion.getVersion() + "]");

						StringBuilder removeScript = removeScriptGenerator.generate(catalogVersion, filteredComposedTypes);

						//	Interceptor is disabled to make it possible to remove categories during removing on import process.
						// Interceptor doesn't allow removing parent category when it has children
						removeScript = new StringBuilder(removeScript.toString().replace(REMOVE_CATEGORY,
								REMOVE_CATEGORY_DISABLE_INTERCEPTOR));

						return removeScript;
					}
				}, userService.getAdminUser()
		);
	}

	private ImportConfig getImpexConfig(final StringBuilder stream)
	{
		final ImpExResource mediaRes = new StreamBasedImpExResource(new ByteArrayInputStream(stream.toString().getBytes()),
				CSVConstants.HYBRIS_ENCODING);
		final ImportConfig config = new ImportConfig();
		config.setRemoveOnSuccess(false);
		config.setScript(mediaRes);
		config.setSynchronous(true);
		return config;
	}

	public ImpexScriptGenerator getRemoveScriptGenerator()
	{
		return removeScriptGenerator;
	}

	@Required
	public void setRemoveScriptGenerator(final ImpexScriptGenerator removeScriptGenerator)
	{
		this.removeScriptGenerator = removeScriptGenerator;
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

	public SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
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

	public ImportService getImportService()
	{
		return importService;
	}

	@Required
	public void setImportService(final ImportService importService)
	{
		this.importService = importService;
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

	public Map<Class, ComposedTypesGenerator> getComposedTypesGeneratorsMap()
	{
		return composedTypesGeneratorsMap;
	}

	@Required
	public void setComposedTypesGeneratorsMap(final Map<Class, ComposedTypesGenerator> composedTypesGeneratorsMap)
	{
		this.composedTypesGeneratorsMap = composedTypesGeneratorsMap;
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

	public TaskInfoService<TaskInfoModel> getTaskInfoService()
	{
		return taskInfoService;
	}

	@Required
	public void setTaskInfoService(final TaskInfoService<TaskInfoModel> taskInfoService)
	{
		this.taskInfoService = taskInfoService;
	}

	public Set<String> getSkipComposedTypeModels()
	{
		return skipComposedTypeModels;
	}

	public void setSkipComposedTypeModels(final Set<String> skipComposedTypeModels)
	{
		this.skipComposedTypeModels = skipComposedTypeModels;
	}
}
