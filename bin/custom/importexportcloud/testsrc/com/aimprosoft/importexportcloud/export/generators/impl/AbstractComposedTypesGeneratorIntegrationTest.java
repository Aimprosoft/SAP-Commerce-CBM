package com.aimprosoft.importexportcloud.export.generators.impl;

import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;

import java.util.Set;

public class AbstractComposedTypesGeneratorIntegrationTest extends ServicelayerTransactionalTest
{
    protected ComposedType findType(Set<ComposedType> typeSet, String typeCode) {
        return typeSet.stream().filter(composedType -> composedType.getCode().equals(typeCode)).findAny().orElse(null);
    }

}
