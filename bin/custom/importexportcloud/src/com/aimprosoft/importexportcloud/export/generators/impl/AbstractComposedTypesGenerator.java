package com.aimprosoft.importexportcloud.export.generators.impl;

import de.hybris.platform.jalo.JaloItemNotFoundException;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.jalo.type.TypeManager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.export.ExportConfigurationService;
import com.aimprosoft.importexportcloud.export.generators.ComposedTypesGenerator;


public abstract class AbstractComposedTypesGenerator implements ComposedTypesGenerator
{
	protected static final Logger LOGGER = Logger.getLogger(AbstractComposedTypesGenerator.class);

	private Collection<ComposedTypesGenerator> generators = Collections.emptyList();

	private ExportConfigurationService exportConfigurationService;

	@Override
	public Set<ComposedType> generateComposedTypes()
	{
		final Set<ComposedType> resultSet = new HashSet<>();

		generators.forEach(generator -> resultSet.addAll(generator.generateComposedTypes()));

		resultSet.addAll(getComposedTypesInternal());

		return resultSet;
	}

	protected abstract Set<ComposedType> getComposedTypesInternal();

	protected boolean isEnum(final ComposedType composedType)
	{
		return composedType.getSuperType().getCode().equals("EnumerationValue");
	}

	protected Set<ComposedType> getComposedTypesFromTypeCodes(final Collection<String> itemTypeCodes)
	{
		final HashSet<ComposedType> result = new HashSet<>();
		final TypeManager typeManager = TypeManager.getInstance();

		for (final String itemTypeCode : itemTypeCodes)
		{
			try
			{
				result.add(typeManager.getComposedType(itemTypeCode));
			}
			catch (final JaloItemNotFoundException e)
			{
				LOGGER.warn(e.getMessage());
			}
		}

		return result;
	}

	public ExportConfigurationService getExportConfigurationService()
	{
		return exportConfigurationService;
	}

	@Required
	public void setExportConfigurationService(final ExportConfigurationService exportConfigurationService)
	{
		this.exportConfigurationService = exportConfigurationService;
	}

	public Collection<ComposedTypesGenerator> getGenerators()
	{
		return generators;
	}

	public void setGenerators(final Collection<ComposedTypesGenerator> generators)
	{
		this.generators = generators;
	}
}
