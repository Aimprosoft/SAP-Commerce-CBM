package com.aimprosoft.importexportcloud.export.generators;

import de.hybris.platform.jalo.type.ComposedType;

import java.util.Set;


public interface ComposedTypesGenerator
{
	/**
	 * Generates set of Composed Types.
	 *
	 * @return set of composed types
	 */
	Set<ComposedType> generateComposedTypes();
}
