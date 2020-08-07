package com.aimprosoft.importexportbackoffice.widgets.state;

import com.hybris.cockpitng.engine.WidgetInstanceManager;
import org.zkoss.zul.Combobox;


public abstract class AbstractWidgetUIState implements WidgetUIState
{
	private static final String CONNECT_STORAGE_CONFIG_BUTTON = "connectStorageConfigButton";
	private static final String DISCONNECT_BUTTON = "disconnectButton";
	private WidgetUIContext widgetUIContext;

	public AbstractWidgetUIState(final WidgetUIContext widgetUIContext)
	{
		this.widgetUIContext = widgetUIContext;
	}

	@Override
	public abstract void renderDropBoxConnectButton(String authURL);

	@Override
	public abstract void renderConnectAndActionsButtons();

	@Override
	public abstract void resetStorageConfigLayout();

	protected void resetConfigLayout()
	{
		((Combobox)getWidgetUIContext().getElement("storageConfigComboBox")).setSelectedItem(null);
		getWidgetUIContext().getButton(CONNECT_STORAGE_CONFIG_BUTTON).setDisabled(true);
		getWidgetUIContext().getButton(CONNECT_STORAGE_CONFIG_BUTTON).setVisible(true);
		getWidgetUIContext().getButton("dropBoxAuthButton").setVisible(false);
		final WidgetInstanceManager widgetInstanceManager = getWidgetUIContext().getWidgetInstanceManager();
		getWidgetUIContext().getButton(CONNECT_STORAGE_CONFIG_BUTTON).setLabel(widgetInstanceManager.getLabel("storageconfig.button.connect"));
		disableConfigActions();
		getWidgetUIContext().getButton(DISCONNECT_BUTTON).setDisabled(true);

		getWidgetUIContext().changeWidgetUIState(new ResetedWidgetUIState(getWidgetUIContext()));
	}

	@Override
	public abstract void renderConnectedButtons();

	public WidgetUIContext getWidgetUIContext()
	{
		return widgetUIContext;
	}

	public void setWidgetUIContext(final WidgetUIContext widgetUIContext)
	{
		this.widgetUIContext = widgetUIContext;
	}

	protected void disableConfigActions()
	{
		getWidgetUIContext().getButton("editStorageConfigButton").setDisabled(true);
		getWidgetUIContext().getButton("removeStorageConfigButton").setDisabled(true);
	}

	protected void setConnectedButtons()
	{
		getWidgetUIContext().getButton(CONNECT_STORAGE_CONFIG_BUTTON).setVisible(true);
		getWidgetUIContext().getButton(CONNECT_STORAGE_CONFIG_BUTTON).setDisabled(true);
		final WidgetInstanceManager widgetInstanceManager = getWidgetUIContext().getWidgetInstanceManager();
		getWidgetUIContext().getButton(CONNECT_STORAGE_CONFIG_BUTTON).setLabel(widgetInstanceManager.getLabel("storageconfig.button.connected"));
		getWidgetUIContext().getButton("dropBoxAuthButton").setVisible(false);
		getWidgetUIContext().getButton(DISCONNECT_BUTTON).setDisabled(false);
		disableConfigActions();

		getWidgetUIContext().changeWidgetUIState(new ConnectedWidgetUIState(getWidgetUIContext()));
	}

	protected void enableActions()
	{
		getWidgetUIContext().getButton("editStorageConfigButton").setDisabled(false);
		getWidgetUIContext().getButton("removeStorageConfigButton").setDisabled(false);
		getWidgetUIContext().getButton(DISCONNECT_BUTTON).setDisabled(true);
	}
}
