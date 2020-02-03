package com.aimprosoft.importexportcloud.service.script.modifiers;

import de.hybris.platform.impex.jalo.exp.ScriptGenerator;


public interface IemScriptModifier
{
	/**
	 * Adjusts script generator for particular needs.
	 *
	 * @param scriptGenerator script generator to adjust
	 */
	void modify(ScriptGenerator scriptGenerator);
}
