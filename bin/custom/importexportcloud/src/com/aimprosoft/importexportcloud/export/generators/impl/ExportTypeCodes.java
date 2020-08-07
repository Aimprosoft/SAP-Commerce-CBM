package com.aimprosoft.importexportcloud.export.generators.impl;

import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.catalog.model.ProductFeatureModel;
import de.hybris.platform.catalog.model.classification.AttributeValueAssignmentModel;
import de.hybris.platform.catalog.model.classification.ClassAttributeAssignmentModel;
import de.hybris.platform.catalog.model.classification.ClassificationAttributeModel;
import de.hybris.platform.catalog.model.classification.ClassificationAttributeUnitModel;
import de.hybris.platform.catalog.model.classification.ClassificationAttributeValueModel;
import de.hybris.platform.catalog.model.classification.ClassificationClassModel;
import de.hybris.platform.catalog.model.classification.ClassificationKeywordModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.cms2.model.contents.CMSItemModel;
import de.hybris.platform.cms2.model.contents.ContentCatalogModel;
import de.hybris.platform.cms2.model.contents.ContentSlotNameModel;
import de.hybris.platform.cms2.model.processing.CMSVersionGCProcessModel;
import de.hybris.platform.cms2.model.relations.CMSRelationModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.commons.model.renderer.RendererTemplateModel;
import de.hybris.platform.core.model.media.MediaContainerModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.VendorModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.solrfacetsearch.model.SolrSearchQueryPropertyModel;
import de.hybris.platform.solrfacetsearch.model.SolrSearchQueryTemplateModel;
import de.hybris.platform.solrfacetsearch.model.SolrSortFieldModel;
import de.hybris.platform.solrfacetsearch.model.SolrSortModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedPropertyModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexerQueryModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrValueRangeModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrValueRangeSetModel;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;


final class ExportTypeCodes
{
	static final Set<String> CMS_RELATED_EXTENSION_NAMES = Sets.newHashSet(
			"cms2",
			"cms2lib",
			"acceleratorcms"
	);

	static final List<String> CONTENT_CATALOG_ASSOCIATED_TYPE_CODES = Arrays.asList(
			MediaModel._TYPECODE,
			MediaContainerModel._TYPECODE,
			RendererTemplateModel._TYPECODE,
			CatalogUnawareMediaModel._TYPECODE
	);

	static final List<String> CONTENT_CATALOG_TYPE_CODES_TO_IGNORE = Arrays.asList(
			CMSSiteModel._TYPECODE,
			ContentCatalogModel._TYPECODE,
			"CatalogsForCMSSite",
			"ValidComponentTypesForSite",
			ContentSlotNameModel._TYPECODE,
			"ValidComponentTypesForContentSlots"
	);

	static final List<String> CMS_ROOT_TYPE_CODES = Arrays.asList(
			CMSItemModel._TYPECODE,
			CMSRelationModel._TYPECODE
	);

	static final List<String> PRODUCT_CATALOG_TYPE_CODES = Arrays.asList(
			MediaModel._TYPECODE,
			MediaContainerModel._TYPECODE,
			ProductModel._TYPECODE,
			CategoryModel._TYPECODE,
			"CategoryCategoryRelation",
			"CategoryProductRelation",
			ProductFeatureModel._TYPECODE,
			StockLevelModel._TYPECODE,
			WarehouseModel._TYPECODE,
			VendorModel._TYPECODE,
			"StockLevelProductRelation",
			"ProductVendorRelation"
	);

	static final List<String> CLASSIFICATION_CATALOG_AND_SOLR_TYPE_CODES = Arrays.asList(
			ClassificationClassModel._TYPECODE,
			ClassificationKeywordModel._TYPECODE,
			ClassificationAttributeUnitModel._TYPECODE,
			ClassAttributeAssignmentModel._TYPECODE,
			ClassificationAttributeModel._TYPECODE,
			ClassificationAttributeValueModel._TYPECODE,
			AttributeValueAssignmentModel._TYPECODE,
			"CategoryCategoryRelation",

			SolrValueRangeSetModel._TYPECODE,
			SolrValueRangeModel._TYPECODE,
			SolrIndexedPropertyModel._TYPECODE,
			SolrSearchQueryPropertyModel._TYPECODE,
			SolrIndexedTypeModel._TYPECODE,
			SolrFacetSearchConfigModel._TYPECODE,
			SolrIndexerQueryModel._TYPECODE,
			SolrSortModel._TYPECODE,
			SolrSortFieldModel._TYPECODE,
			SolrSearchQueryTemplateModel._TYPECODE,
			"SolrFacetSearchConfig2LanguageRelation",
			"SolrFacetSearchConfig2CurrencyRelation",
			"SolrIndexedProperty2SolrValueRangeSetRelation"
	);

	static final List<String> SITE_TYPE_CODES = Arrays.asList(
			CMSSiteModel._TYPECODE,
			"SolrFacetSearchConfig2CatalogVersionRelation"
	);

	static final Set<String> UNUSED_GC_PROCESSES = Sets.newHashSet(
			CMSVersionGCProcessModel._TYPECODE,
			"CMSVersionGCProcess2CMSVersion"
	);

	private ExportTypeCodes()
	{
	}
}
