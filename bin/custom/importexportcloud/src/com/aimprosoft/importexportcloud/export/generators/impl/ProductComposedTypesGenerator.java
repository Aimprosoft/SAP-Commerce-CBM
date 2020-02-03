package com.aimprosoft.importexportcloud.export.generators.impl;

import de.hybris.platform.jalo.type.ComposedType;

import java.util.HashSet;
import java.util.Set;

import static com.aimprosoft.importexportcloud.export.generators.impl.ExportTypeCodes.PRODUCT_CATALOG_TYPE_CODES;


public class ProductComposedTypesGenerator extends AbstractComposedTypesGenerator
{
	@Override
	protected Set<ComposedType> getComposedTypesInternal()
	{
		HashSet<ComposedType> result = new HashSet<>();

		result.addAll(getComposedTypesFromTypeCodes(PRODUCT_CATALOG_TYPE_CODES));
		result.addAll(getComposedTypesFromTypeCodes(getExportConfigurationService().getProductAdditionalTypes()));

		return result;
	}
}
