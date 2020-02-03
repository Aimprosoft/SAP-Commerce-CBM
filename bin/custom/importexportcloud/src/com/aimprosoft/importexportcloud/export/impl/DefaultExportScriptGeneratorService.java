package com.aimprosoft.importexportcloud.export.impl;

import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.impex.jalo.exp.generator.ExportScriptGenerator;
import de.hybris.platform.impex.jalo.exp.generator.ScriptModifier;
import de.hybris.platform.jalo.c2l.Language;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.enums.TaskInfoScope;
import com.aimprosoft.importexportcloud.export.ExportConfigurationService;
import com.aimprosoft.importexportcloud.export.ExportScriptGeneratorService;
import com.aimprosoft.importexportcloud.export.generators.ComposedTypesGenerator;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;


public class DefaultExportScriptGeneratorService implements ExportScriptGeneratorService
{
	private static final Logger LOGGER = Logger.getLogger(DefaultExportScriptGeneratorService.class);

	private ModelService modelService;

	private CommonI18NService commonI18NService;

	private ScriptModifier noMediaScriptModifier;

	private ScriptModifier mediaScriptModifier;

	private ExportConfigurationService exportConfigurationService;

	private Map<Class, ComposedTypesGenerator> composedTypesGeneratorsMap;

	@Override
	public String generateExportScript(final ExportTaskInfoModel exportTaskInfoModel)
	{
		final ExportScriptGenerator generator = getConfiguredExportScriptGenerator(exportTaskInfoModel);
		final String generatedScript = generator.generateScript();

		LOGGER.info("Export script was generated successfully.");

		return getScriptWithoutPKs(generatedScript);
	}

	private String getScriptWithoutPKs(final String script)
	{
		return script.replace(";pk;", ";");
	}

	private ExportScriptGenerator getConfiguredExportScriptGenerator(final ExportTaskInfoModel exportTaskInfoModel)
	{
		final ExportScriptGenerator generator = new ExportScriptGenerator();

		generator.useDocumentID(false);
		generator.includeSystemTypes(false);

		final ScriptModifier scriptModifier = BooleanUtils.isTrue(exportTaskInfoModel.isExportMediaNeeded()) ?
				getMediaScriptModifier() :
				getNoMediaScriptModifier();

		generator.registerScriptModifier(scriptModifier);

		generator.setLanguages(getLanguagesToPutInScript());
		generator.setTypes(getComposedTypesForScope(exportTaskInfoModel));

		return generator;
	}

	private Set<ComposedType> getComposedTypesForScope(final ExportTaskInfoModel exportTaskInfoModel)
	{
		Set<ComposedType> composedTypes = null;
		final TaskInfoScope taskScope = exportTaskInfoModel.getTaskScope();

		if (taskScope.equals(TaskInfoScope.SITESCOPE))
		{
			final ComposedTypesGenerator composedTypesGenerator = composedTypesGeneratorsMap.get(CMSSiteModel.class);
			composedTypes = composedTypesGenerator.generateComposedTypes();
		}
		else if (taskScope.equals(TaskInfoScope.CATALOGSCOPE))
		{
			final CatalogModel catalog = exportTaskInfoModel.getCatalogVersion().getCatalog();
			final ComposedTypesGenerator composedTypesGenerator = composedTypesGeneratorsMap.get(catalog.getClass());
			composedTypes = composedTypesGenerator.generateComposedTypes();
		}
		else if (taskScope.equals(TaskInfoScope.ITEMSCOPE))
		{
			composedTypes = Collections.emptySet();
		}

		return composedTypes;
	}

	private Set<Language> getLanguagesToPutInScript()
	{
		return commonI18NService.getAllLanguages().stream()
				.map(languageModel -> modelService.<Language>getSource(languageModel))
				.collect(Collectors.toSet());
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

	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	public ExportConfigurationService getExportConfigurationService()
	{
		return exportConfigurationService;
	}

	@Required
	public void setExportConfigurationService(
			final ExportConfigurationService exportConfigurationService)
	{
		this.exportConfigurationService = exportConfigurationService;
	}

	public Map<Class, ComposedTypesGenerator> getComposedTypesGeneratorsMap()
	{
		return composedTypesGeneratorsMap;
	}

	@Required
	public void setComposedTypesGeneratorsMap(
			final Map<Class, ComposedTypesGenerator> composedTypesGeneratorsMap)
	{
		this.composedTypesGeneratorsMap = composedTypesGeneratorsMap;
	}

	public ScriptModifier getMediaScriptModifier()
	{
		return mediaScriptModifier;
	}

	@Required
	public void setMediaScriptModifier(final ScriptModifier mediaScriptModifier)
	{
		this.mediaScriptModifier = mediaScriptModifier;
	}

	public ScriptModifier getNoMediaScriptModifier()
	{
		return noMediaScriptModifier;
	}

	@Required
	public void setNoMediaScriptModifier(final ScriptModifier noMediaScriptModifier)
	{
		this.noMediaScriptModifier = noMediaScriptModifier;
	}


}
