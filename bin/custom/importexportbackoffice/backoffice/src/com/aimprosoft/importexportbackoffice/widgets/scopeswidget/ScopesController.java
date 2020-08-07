package com.aimprosoft.importexportbackoffice.widgets.scopeswidget;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;

import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.util.DefaultWidgetController;


public class ScopesController extends DefaultWidgetController //NOSONAR
{
	private static final Logger LOG = Logger.getLogger(ScopesController.class);
	private static final String OUTPUT_SOCKET_SELECTED_SCOPE = "selectedScope";
	private static final String OUTPUT_SOCKET_EXPORT_MEDIA_NEEDED = "outputSelectedExportMediaNeeded";
	private static final String SELECTED_SCOPE_INDEX = "selectedScopeIndex";
	private static final String IS_MEDIA_NEEDED = "isMediaNeeded";

	private Radiogroup scopeConfigRadiogroup;
	private Checkbox exportMediaNeededCheckBox;

	@Override
	public void initialize(final Component comp)
	{
		super.initialize(comp);

		final Integer selectedScopeIndex = getSelectedScopeIndex();

		if (selectedScopeIndex != null)
		{
			scopeConfigRadiogroup.setSelectedIndex(selectedScopeIndex);
		}

		final Boolean exportMediaNeededFlag = getExportMediaNeededFlag();

		if (exportMediaNeededFlag != null)
		{
			exportMediaNeededCheckBox.setChecked(exportMediaNeededFlag);
		}
	}

	@SocketEvent(socketId = "reset")
	public void onReset(final StorageConfigData storageConfigData)
	{
		scopeConfigRadiogroup.setSelectedIndex(0);
		exportMediaNeededCheckBox.setChecked(Boolean.TRUE);
	}

	@ViewEvent(componentID = "scopeConfigRadiogroup", eventName = Events.ON_CHECK)
	public void onChangeScope(final CheckEvent event)
	{
		final String inputSelectedScope = ((Radio) event.getTarget()).getValue();
		if (StringUtils.isNotBlank(inputSelectedScope))
		{
			sendOutput(OUTPUT_SOCKET_SELECTED_SCOPE, inputSelectedScope);
			setSelectedScopeIndex(scopeConfigRadiogroup.getSelectedIndex());
		}
		else
		{
			LOG.error("This Scope is not exist or empty: " + inputSelectedScope);
		}
	}

	@ViewEvent(componentID = "exportMediaNeededCheckBox", eventName = Events.ON_CHECK)
	public void onCheckExportMediaNeeded(final CheckEvent event)
	{
		final boolean checked = exportMediaNeededCheckBox.isChecked();
		setExportMediaNeededFlag(checked);
		sendOutput(OUTPUT_SOCKET_EXPORT_MEDIA_NEEDED, checked);
	}

	private Integer getSelectedScopeIndex()
	{
		return this.getWidgetInstanceManager().getModel().getValue(SELECTED_SCOPE_INDEX, Integer.class);
	}

	private void setSelectedScopeIndex(final Integer selectedScope)
	{
		this.getWidgetInstanceManager().getModel().put(SELECTED_SCOPE_INDEX, selectedScope);
	}

	private Boolean getExportMediaNeededFlag()
	{
		return this.getWidgetInstanceManager().getModel().getValue(IS_MEDIA_NEEDED, Boolean.class);
	}

	private void setExportMediaNeededFlag(final Boolean isMediaExported)
	{
		this.getWidgetInstanceManager().getModel().put(IS_MEDIA_NEEDED, isMediaExported);
	}
}
