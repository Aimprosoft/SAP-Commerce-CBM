package com.aimprosoft.importexportbackoffice.renderers;

import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;

import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.util.UITools;
import com.hybris.cockpitng.widgets.collectionbrowser.mold.impl.listview.renderer.DefaultListCellRenderer;


public class ExportTaskInfoNullListCellRenderer extends DefaultListCellRenderer
{

	private static final String EMPTY_LABEL_TEXT = "-";

	@Override
	public void render(final Listcell parent, final ListColumn columnConfiguration, final Object object, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManger)
	{
		final Object property = ((ExportTaskInfoModel) object).getProperty(columnConfiguration.getQualifier());

		if (property == null)
		{
			final Label label = new Label(EMPTY_LABEL_TEXT);
			UITools.modifySClass(label, "yw-listview-cell-label", true);
			label.setAttribute("hyperlink-candidate", Boolean.TRUE);
			parent.appendChild(label);
			fireComponentRendered(label, parent, columnConfiguration, object);
			fireComponentRendered(parent, columnConfiguration, object);
		}
		else
		{
			super.render(parent, columnConfiguration, object, dataType, widgetInstanceManger);
		}
	}
}
