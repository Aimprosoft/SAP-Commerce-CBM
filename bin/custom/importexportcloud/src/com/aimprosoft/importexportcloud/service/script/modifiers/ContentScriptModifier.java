package com.aimprosoft.importexportcloud.service.script.modifiers;

import de.hybris.platform.impex.jalo.exp.ScriptGenerator;


public class ContentScriptModifier implements IemScriptModifier
{
	@Override
	public void modify(ScriptGenerator scriptGenerator)
	{
		scriptGenerator.addReplacedColumnExpression("RendererTemplate", "content", "code");
		scriptGenerator.addReplacedColumnExpression("CMSNavigationEntry", "item", "CMSItem.uid, CMSItem.catalogVersion(catalog(id),version)");
	}
}
