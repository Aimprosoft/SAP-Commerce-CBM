package com.aimprosoft.importexportbackoffice.editor.customtext;

import org.zkoss.zul.Textbox;

import com.hybris.cockpitng.editor.defaulttext.DefaultTextEditor;
import com.hybris.cockpitng.editors.EditorContext;


public class CustomMaskedTextEditor extends DefaultTextEditor
{
	@Override
	protected void initAdditionalParameters(final Textbox editorView, final EditorContext<String> context)
	{
		super.initAdditionalParameters(editorView, context);
		editorView.setType("password");
	}
}
