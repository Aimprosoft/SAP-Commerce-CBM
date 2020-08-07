package com.aimprosoft.importexportcloud.export.generators.impl;

import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.impex.jalo.ImpExException;
import de.hybris.platform.jalo.type.ComposedType;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Set;

import static org.junit.Assert.*;

public class SiteComposedTypesGeneratorIntegrationTest extends AbstractComposedTypesGeneratorIntegrationTest
{
    @Resource
    private SiteComposedTypesGenerator siteComposedTypesGenerator;

    @Test
    public void getComposedTypes()
    {
        Set<ComposedType> typeSet = siteComposedTypesGenerator.getComposedTypesInternal();
        assertTrue(typeSet.size() != 0);

        ComposedType unexpectedType = typeSet.stream().filter(composedType -> composedType.getCode().equals(ProductModel._TYPECODE)).findAny().orElse(null);

        assertNotNull(findType(typeSet, CMSSiteModel._TYPECODE));
        assertNull(unexpectedType);
    }

    @Test
    public void withDefaultConfiguration() throws ImpExException
    {
        importCsv("/test/test-export-configuration.impex", "UTF-8");

        Set<ComposedType> typeSet = siteComposedTypesGenerator.getComposedTypesInternal();
        ComposedType expectedType = typeSet.stream().filter(composedType -> composedType.getCode().equals(ProductModel._TYPECODE)).findAny().orElse(null);
        assertNotNull(expectedType);
    }
}
