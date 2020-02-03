package com.aimprosoft.importexportcloud.export.generators.impl;

import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.jalo.type.TypeManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.aimprosoft.importexportcloud.export.generators.impl.ExportTypeCodes.*;


public class ContentComposedTypesGenerator extends AbstractComposedTypesGenerator
{
	@Override
	protected Set<ComposedType> getComposedTypesInternal()
	{
		Set<ComposedType> result = new HashSet<>();

		result.addAll(getCMSBasedComposedTypes());
		result.addAll(getComposedTypesFromCmsRelatedExtensions());
		result.addAll(getComposedTypesFromTypeCodes(CONTENT_CATALOG_ASSOCIATED_TYPE_CODES));

		result.removeAll(getComposedTypesFromTypeCodes(CONTENT_CATALOG_TYPE_CODES_TO_IGNORE));

		result.addAll(getComposedTypesFromTypeCodes(getExportConfigurationService().getContentAdditionalTypes()));

		return result;
	}

	private Set<ComposedType> getCMSBasedComposedTypes()
	{
		TypeManager typeManager = TypeManager.getInstance();

		return CMS_ROOT_TYPE_CODES.stream()
				.map(rootTypeCode -> typeManager.getComposedType(rootTypeCode).getAllSubTypes())
				.flatMap(Collection::stream)
				.filter(composedType -> !composedType.isAbstract()).collect(Collectors.toSet());
	}

	private Set<ComposedType> getComposedTypesFromCmsRelatedExtensions()
	{
		Set<? extends ComposedType> allComposedTypes = TypeManager.getInstance().getAllComposedTypes();

		return allComposedTypes.stream()
				.filter(composedType -> !composedType.isAbstract() && !isEnum(composedType)
						&& (CMS_RELATED_EXTENSION_NAMES.contains(composedType.getExtensionName())))
				.collect(Collectors.toSet());
	}
}
