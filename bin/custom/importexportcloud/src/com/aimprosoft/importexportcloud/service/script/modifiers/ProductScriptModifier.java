package com.aimprosoft.importexportcloud.service.script.modifiers;

import de.hybris.platform.europe1.jalo.impex.Europe1PricesTranslator;
import de.hybris.platform.europe1.jalo.impex.Europe1ProductDiscountTranslator;
import de.hybris.platform.impex.jalo.exp.ScriptGenerator;


public class ProductScriptModifier implements IemScriptModifier
{
	@Override
	public void modify(ScriptGenerator scriptGenerator)
	{
		scriptGenerator.addAdditionalModifier("Product", "europe1prices", "translator", Europe1PricesTranslator.class.getName());//NOSONAR
		scriptGenerator.addAdditionalModifier("Product", "europe1Discounts", "translator", Europe1ProductDiscountTranslator.class.getName());//NOSONAR
		scriptGenerator.addIgnoreColumn("Product", "europe1Taxes");//NOSONAR

		scriptGenerator.addIgnoreColumn("ProductFeature", "classificationAttributeAssignment");
		scriptGenerator.addAdditionalColumn("ProductFeature",
				"classificationAttributeAssignment(classificationAttribute(code,systemVersion(catalog(id),version)),classificationClass(catalogVersion(catalog(id),version),code),systemVersion(catalog(id),version))");

		scriptGenerator.addIgnoreColumn("StockLevel", "product");//NOSONAR
		scriptGenerator.addReplacedColumnExpression("StockLevel", "inStockStatus", "code");//NOSONAR
		scriptGenerator.addReplacedColumnExpression("StockLevel", "warehouse", "code");//NOSONAR
		scriptGenerator.addAdditionalModifier("StockLevel", "warehouse", "unique", "true");//NOSONAR
		scriptGenerator.addAdditionalModifier("StockLevel", "productCode", "unique", "true");//NOSONAR

		scriptGenerator.addAdditionalModifier("Warehouse", "code", "unique", "true");//NOSONAR
		scriptGenerator.addReplacedColumnExpression("Warehouse", "vendor", "code");//NOSONAR
		scriptGenerator.addIgnoreColumn("Warehouse", "default");//NOSONAR
		scriptGenerator.addAdditionalColumn("Warehouse", "default");//NOSONAR
		scriptGenerator.addIgnoreColumn("Warehouse", "vendor");//NOSONAR
		scriptGenerator.addAdditionalColumn("Warehouse", "vendor(code)");

		scriptGenerator.addAdditionalModifier("Vendor", "code", "unique", "true");
		scriptGenerator.addReplacedColumnExpression("ProductVendorRelation", "target", "code");
		scriptGenerator.addReplacedColumnExpression("StockLevelProductRelation", "source", "productCode,warehouse(code)");
	}
}
