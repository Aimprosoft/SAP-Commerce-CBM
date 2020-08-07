package com.aimprosoft.importexportcloud.service.script.modifiers;

import de.hybris.platform.impex.jalo.exp.ScriptGenerator;


public class SolrScriptModifier implements IemScriptModifier //NOSONAR
{
	@Override
	public void modify(final ScriptGenerator scriptGenerator)
	{
		scriptGenerator.addAdditionalModifier("SolrValueRange", "name", "unique", "true");//NOSONAR
		scriptGenerator.addAdditionalModifier("SolrValueRange", "solrValueRangeSet", "unique", "true");//NOSONAR

		scriptGenerator.addAdditionalModifier("SolrIndexedProperty", "name", "unique", "true");//NOSONAR
		scriptGenerator.addAdditionalModifier("SolrIndexedProperty", "solrIndexedType", "unique", "true");//NOSONAR
		scriptGenerator.addReplacedColumnExpression("SolrIndexedProperty", "solrIndexedType", "identifier");//NOSONAR

		scriptGenerator
				.addReplacedColumnExpression("SolrSearchQueryProperty", "indexedProperty", "name, solrIndexedType(identifier)");//NOSONAR
		scriptGenerator
				.addReplacedColumnExpression("SolrSearchQueryProperty", "searchQueryTemplate", "name, indexedType(identifier)");

		scriptGenerator.addReplacedColumnExpression("SolrSort", "indexedType", "identifier");//NOSONAR

		scriptGenerator.addReplacedColumnExpression("SolrSearchQueryTemplate", "indexedType", "identifier");//NOSONAR

		scriptGenerator.addReplacedColumnExpression("SolrSortField", "sort", "indexedType(identifier),code");

		scriptGenerator.addReplacedColumnExpression("SolrIndexerQuery", "solrIndexedType", "identifier");//NOSONAR
		scriptGenerator.addIgnoreColumn("SolrIndexerQuery", "type");//NOSONAR
		scriptGenerator.addAdditionalColumn("SolrIndexerQuery", "type(code)");

		scriptGenerator.addReplacedColumnExpression("SolrFacetSearchConfig", "solrSearchConfig", "description");//NOSONAR
		scriptGenerator.addIgnoreColumn("SolrFacetSearchConfig", "languageKeywordRedirectMapping");//NOSONAR
		scriptGenerator.addIgnoreColumn("SolrFacetSearchConfig", "languageStopWordMapping");//NOSONAR
		scriptGenerator.addIgnoreColumn("SolrFacetSearchConfig", "languageSynonymMapping");//NOSONAR

		scriptGenerator
				.addReplacedColumnExpression("SolrSearchQueryTemplate", "groupProperty", "name, solrIndexedType(identifier)");

		scriptGenerator.addReplacedColumnExpression("SolrIndexedProperty2SolrValueRangeSetRelation", "source",
				"name, solrIndexedType(identifier)");
	}
}
