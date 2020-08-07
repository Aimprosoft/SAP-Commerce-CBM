package com.aimprosoft.importexportcloud.export.generators.impl;

import de.hybris.platform.acceleratorcms.model.actions.ViewOrderActionModel;
import de.hybris.platform.acceleratorcms.model.components.*;
import de.hybris.platform.cms2.model.CMSComponentTypeModel;
import de.hybris.platform.cms2.model.CMSPageTypeModel;
import de.hybris.platform.cms2.model.ComponentTypeGroupModel;
import de.hybris.platform.cms2.model.contents.components.CMSImageComponentModel;
import de.hybris.platform.cms2.model.pages.CategoryPageModel;
import de.hybris.platform.cms2.model.pages.PageTemplateModel;
import de.hybris.platform.cms2.model.restrictions.CMSCampaignRestrictionModel;
import de.hybris.platform.cms2lib.model.components.ProductListComponentModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.impex.jalo.ImpExException;
import de.hybris.platform.jalo.type.ComposedType;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ContentComposedTypesGeneratorIntegrationTest extends AbstractComposedTypesGeneratorIntegrationTest
{
    @Resource
    private ContentComposedTypesGenerator contentComposedTypesGenerator;

    @Test
    public void getComposedTypes()
    {
        Set<ComposedType> typeSet = contentComposedTypesGenerator.getComposedTypesInternal();
        Assert.assertTrue(typeSet.size() != 0);

        ComposedType unexpectedType = findType(typeSet, ProductModel._TYPECODE);

        assertNotNull(findType(typeSet, CMSPageTypeModel._TYPECODE));
        assertNotNull(findType(typeSet, ProductRefinementComponentModel._TYPECODE));
        assertNotNull(findType(typeSet, AccountBookmarkComponentModel._TYPECODE));
        assertNotNull(findType(typeSet, CMSProductListComponentModel._TYPECODE));
        assertNotNull(findType(typeSet, CMSCampaignRestrictionModel._TYPECODE));
        assertNotNull(findType(typeSet, ViewOrderActionModel._TYPECODE));
        assertNotNull(findType(typeSet, CMSImageComponentModel._TYPECODE));
        assertNotNull(findType(typeSet, PageTemplateModel._TYPECODE));

        assertNotNull(findType(typeSet, ProductListComponentModel._TYPECODE));
        assertNotNull(findType(typeSet, ProductRefinementComponentModel._TYPECODE));
        assertNotNull(findType(typeSet, CMSComponentTypeModel._TYPECODE));
        assertNotNull(findType(typeSet, CategoryPageModel._TYPECODE));
        assertNotNull(findType(typeSet, FooterNavigationComponentModel._TYPECODE));
        assertNotNull(findType(typeSet, MiniCartComponentModel._TYPECODE));
        assertNotNull(findType(typeSet, MediaModel._TYPECODE));
        assertNotNull(findType(typeSet, ComponentTypeGroupModel._TYPECODE));

        assertNull(unexpectedType);
    }

    @Test
    public void withDefaultConfiguration() throws ImpExException
    {
        importCsv("/test/test-export-configuration.impex", "UTF-8");

        Set<ComposedType> typeSet = contentComposedTypesGenerator.getComposedTypesInternal();
        ComposedType expectedType = typeSet.stream().filter(composedType -> composedType.getCode().equals(ProductModel._TYPECODE)).findAny().orElse(null);
        assertNotNull(expectedType);
    }
}
