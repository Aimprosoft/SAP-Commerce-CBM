package com.aimprosoft.importexportcloud.export.generators.impl;

import de.hybris.platform.catalog.model.classification.*;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.impex.jalo.ImpExException;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.solrfacetsearch.model.SolrSearchQueryPropertyModel;
import de.hybris.platform.solrfacetsearch.model.SolrSearchQueryTemplateModel;
import de.hybris.platform.solrfacetsearch.model.SolrSortFieldModel;
import de.hybris.platform.solrfacetsearch.model.SolrSortModel;
import de.hybris.platform.solrfacetsearch.model.config.*;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Set;

import static org.junit.Assert.*;

public class ClassificationComposedTypesGeneratorIntegrationTest extends AbstractComposedTypesGeneratorIntegrationTest
{

    @Resource
    private ClassificationComposedTypesGenerator classificationComposedTypesGenerator;

    @Test
    public void getComposedTypes()
    {
        Set<ComposedType> typeSet = classificationComposedTypesGenerator.getComposedTypesInternal();
        assertTrue(typeSet.size() != 0);
        ComposedType unexpectedType = findType(typeSet, ProductModel._TYPECODE);

        assertNotNull(findType(typeSet, ClassificationClassModel._TYPECODE));
        assertNotNull(findType(typeSet, SolrSearchQueryTemplateModel._TYPECODE));
        assertNotNull(findType(typeSet, SolrFacetSearchConfigModel._TYPECODE));
        assertNotNull(findType(typeSet, SolrValueRangeSetModel._TYPECODE));
        assertNotNull(findType(typeSet, SolrSortModel._TYPECODE));
        assertNotNull(findType(typeSet, SolrSearchQueryPropertyModel._TYPECODE));
        assertNotNull(findType(typeSet, SolrValueRangeModel._TYPECODE));
        assertNotNull(findType(typeSet, ClassAttributeAssignmentModel._TYPECODE));

        assertNotNull(findType(typeSet, ClassificationAttributeModel._TYPECODE));
        assertNotNull(findType(typeSet, SolrIndexerQueryModel._TYPECODE));
        assertNotNull(findType(typeSet, ClassificationAttributeUnitModel._TYPECODE));
        assertNotNull(findType(typeSet, AttributeValueAssignmentModel._TYPECODE));
        assertNotNull(findType(typeSet, SolrIndexedPropertyModel._TYPECODE));
        assertNotNull(findType(typeSet, SolrIndexedTypeModel._TYPECODE));
        assertNotNull(findType(typeSet, ClassificationAttributeValueModel._TYPECODE));
        assertNotNull(findType(typeSet, SolrSortFieldModel._TYPECODE));

        assertNull(unexpectedType);
    }

    @Test
    public void withDefaultConfiguration() throws ImpExException
    {
        importCsv("/test/test-export-configuration.impex", "UTF-8");

        Set<ComposedType> typeSet = classificationComposedTypesGenerator.getComposedTypesInternal();
        ComposedType expectedType = typeSet.stream().filter(composedType -> composedType.getCode().equals(ProductModel._TYPECODE)).findAny().orElse(null);
        assertNotNull(expectedType);
    }
}
