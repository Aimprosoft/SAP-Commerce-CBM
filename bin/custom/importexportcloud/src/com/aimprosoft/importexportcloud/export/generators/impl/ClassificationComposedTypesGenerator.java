package com.aimprosoft.importexportcloud.export.generators.impl;

import de.hybris.platform.jalo.type.ComposedType;

import java.util.HashSet;
import java.util.Set;

import static com.aimprosoft.importexportcloud.export.generators.impl.ExportTypeCodes.CLASSIFICATION_CATALOG_AND_SOLR_TYPE_CODES;


public class ClassificationComposedTypesGenerator extends AbstractComposedTypesGenerator
{
	@Override
	protected Set<ComposedType> getComposedTypesInternal()
	{
		final HashSet<ComposedType> result = new HashSet<>();

		result.addAll(getComposedTypesFromTypeCodes(CLASSIFICATION_CATALOG_AND_SOLR_TYPE_CODES));
		result.addAll(getComposedTypesFromTypeCodes(getExportConfigurationService().getClassificationAdditionalTypes()));

		return result;
	}
}
