package com.aimprosoft.importexportcloud.export.generators.impl;

import de.hybris.platform.catalog.model.ProductFeatureModel;
import de.hybris.platform.catalog.model.classification.ClassificationClassModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.media.MediaContainerModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.impex.jalo.ImpExException;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.VendorModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ProductComposedTypesGeneratorIntegrationTest extends AbstractComposedTypesGeneratorIntegrationTest
{
    @Resource
    private ProductComposedTypesGenerator productComposedTypesGenerator;

    @Test
    public void getComposedTypes()
    {
        Set<ComposedType> typeSet = productComposedTypesGenerator.getComposedTypesInternal();
        Assert.assertTrue(typeSet.size() != 0);

        ComposedType unexpectedType = findType(typeSet, ClassificationClassModel._TYPECODE);

        assertNotNull(findType(typeSet, MediaModel._TYPECODE));
        assertNotNull(findType(typeSet, StockLevelModel._TYPECODE));
        assertNotNull(findType(typeSet, WarehouseModel._TYPECODE));
        assertNotNull(findType(typeSet, CategoryModel._TYPECODE));
        assertNotNull(findType(typeSet, ProductFeatureModel._TYPECODE));
        assertNotNull(findType(typeSet, VendorModel._TYPECODE));
        assertNotNull(findType(typeSet, ProductModel._TYPECODE));
        assertNotNull(findType(typeSet, MediaContainerModel._TYPECODE));

        assertNull(unexpectedType);
    }

    @Test
    public void withDefaultConfiguration() throws ImpExException
    {
        importCsv("/test/test-export-configuration.impex", "UTF-8");

        Set<ComposedType> typeSet = productComposedTypesGenerator.getComposedTypesInternal();
        ComposedType expectedType = typeSet.stream().filter(composedType -> composedType.getCode().equals(CMSSiteModel._TYPECODE)).findAny().orElse(null);
        Assert.assertNotNull(expectedType);
    }
}
