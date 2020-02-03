package com.aimprosoft.importexportcloud.export.generators.impl;

import de.hybris.platform.jalo.type.ComposedType;

import java.util.HashSet;
import java.util.Set;

import static com.aimprosoft.importexportcloud.export.generators.impl.ExportTypeCodes.SITE_TYPE_CODES;


public class SiteComposedTypesGenerator extends AbstractComposedTypesGenerator
{
	@Override
	protected Set<ComposedType> getComposedTypesInternal()
	{
		HashSet<ComposedType> result = new HashSet<>();

		result.addAll(getComposedTypesFromTypeCodes(SITE_TYPE_CODES));
		result.addAll(getComposedTypesFromTypeCodes(getExportConfigurationService().getSiteAdditionalTypes()));

		return result;
	}
}
