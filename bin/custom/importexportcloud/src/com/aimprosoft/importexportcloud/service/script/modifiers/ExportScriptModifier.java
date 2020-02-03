package com.aimprosoft.importexportcloud.service.script.modifiers;

import de.hybris.platform.impex.jalo.exp.ScriptGenerator;
import de.hybris.platform.impex.jalo.exp.generator.ScriptModifier;
import de.hybris.platform.impex.jalo.media.MediaDataTranslator;
import de.hybris.platform.jalo.type.ComposedType;

import java.util.Set;

import org.springframework.beans.factory.annotation.Required;


public class ExportScriptModifier implements ScriptModifier
{
	private boolean isExportMedia;

	private Set<IemScriptModifier> iemScriptModifiers;

	@Override
	public void init(final ScriptGenerator scriptGenerator)
	{
		iemScriptModifiers.forEach(iemScriptModifier -> iemScriptModifier.modify(scriptGenerator));

		resolveExportMedia(scriptGenerator);
	}

	private void resolveExportMedia(final ScriptGenerator scriptGenerator)
	{
		if (isExportMedia())
		{
			scriptGenerator.addSpecialColumn("Media", "media[translator=" + MediaDataTranslator.class.getName() + ']');
		}
	}

	public boolean isExportMedia()
	{
		return isExportMedia;
	}

	@Required
	public void setExportMedia(final boolean exportMedia)
	{
		isExportMedia = exportMedia;
	}

	public Set<IemScriptModifier> getIemScriptModifiers()
	{
		return iemScriptModifiers;
	}

	@Required
	public void setIemScriptModifiers(final Set<IemScriptModifier> iemScriptModifiers)
	{
		this.iemScriptModifiers = iemScriptModifiers;
	}

	@Override
	public Set<ComposedType> collectSubtypesWithOwnDeployment(final ComposedType composedType)
	{
		throw throwUnsupportedOperationException();
	}

	@Override
	public boolean filterTypeCompletely(final ComposedType composedType)
	{
		throw throwUnsupportedOperationException();
	}

	@Override
	public Set<ComposedType> getExportableRootTypes(final ScriptGenerator scriptGenerator)
	{
		throw throwUnsupportedOperationException();
	}

	private UnsupportedOperationException throwUnsupportedOperationException()
	{
		return new UnsupportedOperationException(
				"Isn't used for script generation. See MigrationScriptModifier class for details.");
	}
}
