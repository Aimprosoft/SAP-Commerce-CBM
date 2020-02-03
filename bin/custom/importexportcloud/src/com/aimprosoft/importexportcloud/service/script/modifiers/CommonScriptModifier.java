package com.aimprosoft.importexportcloud.service.script.modifiers;

import de.hybris.platform.impex.jalo.exp.ScriptGenerator;


public class CommonScriptModifier implements IemScriptModifier
{
	@Override
	public void modify(ScriptGenerator scriptGenerator)
	{
		scriptGenerator.addIgnoreColumn("ComposedType", "superType");
		scriptGenerator.addIgnoreColumn("Item", "owner");
		scriptGenerator.addIgnoreColumn("Item", "creationtime");
		scriptGenerator.addIgnoreColumn("Item", "modifiedtime");
	}
}
