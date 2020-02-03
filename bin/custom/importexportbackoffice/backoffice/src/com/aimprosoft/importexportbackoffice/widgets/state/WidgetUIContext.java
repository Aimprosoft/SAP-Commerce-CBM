package com.aimprosoft.importexportbackoffice.widgets.state;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Button;
import org.zkoss.zul.impl.InputElement;

import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


public class WidgetUIContext
{
	private WidgetUIState widgetUIState;

	private WidgetInstanceManager widgetInstanceManager;

	private StorageConfigData storageConfigData;

	private StorageTypeData storageTypeData;

	private HttpSession httpSession;

	private boolean isResetNeeded;

	private final Map<String, Button> buttons;
	private final Map<String, InputElement> elementsMap;
	private final Map<String, Component> componentsMap;

	public WidgetUIContext(final WidgetInstanceManager widgetInstanceManager)
	{
		this.widgetInstanceManager = widgetInstanceManager;
		widgetUIState = new ResetedWidgetUIState(this);
		buttons = new HashMap<>();
		elementsMap = new HashMap<>();
		componentsMap = new HashMap<>();
	}

	public void renderDropBoxConnectButton(final String authURL)
	{
		widgetUIState.renderDropBoxConnectButton(authURL);
	}

	public void renderConnectAndActionsButtons()
	{
		widgetUIState.renderConnectAndActionsButtons();
	}

	public void resetStorageConfigLayout()
	{
		widgetUIState.resetStorageConfigLayout();
	}

	public void renderConnectedButtons()
	{
		widgetUIState.renderConnectedButtons();
	}

	public void renderConfigComboBox(final StorageConfigFacade storageConfigFacade, final StorageTypeData storageTypeData)
	{
		widgetUIState.renderConfigComboBox(storageConfigFacade, storageTypeData);
	}

	public void changeWidgetUIState(final WidgetUIState widgetUIState)
	{
		this.widgetUIState = widgetUIState;
	}

	public void addButton(final String key, final Button button)
	{
		buttons.putIfAbsent(key, button);
	}

	public Button getButton(final String key)
	{
		return buttons.get(key);
	}

	public void addElement(final String key, final InputElement element)
	{
		elementsMap.putIfAbsent(key, element);
	}

	public InputElement getElement(final String key)
	{
		return elementsMap.get(key);
	}

	public void addComponent(final String key, final Component element)
	{
		componentsMap.putIfAbsent(key, element);
	}

	public Component getComponent(final String key)
	{
		return componentsMap.get(key);
	}

	public WidgetUIState getWidgetUIState()
	{
		return widgetUIState;
	}

	public void setWidgetUIState(final WidgetUIState widgetUIState)
	{
		this.widgetUIState = widgetUIState;
	}

	public WidgetInstanceManager getWidgetInstanceManager()
	{
		return widgetInstanceManager;
	}

	public void setWidgetInstanceManager(final WidgetInstanceManager widgetInstanceManager)
	{
		this.widgetInstanceManager = widgetInstanceManager;
	}

	public StorageConfigData getStorageConfigData()
	{
		return storageConfigData;
	}

	public void setStorageConfigData(final StorageConfigData storageConfigData)
	{
		this.storageConfigData = storageConfigData;
	}

	public StorageTypeData getStorageTypeData()
	{
		return storageTypeData;
	}

	public void setStorageTypeData(final StorageTypeData storageTypeData)
	{
		this.storageTypeData = storageTypeData;
	}

	public HttpSession getHttpSession()
	{
		return httpSession;
	}

	public void setHttpSession(final HttpSession httpSession)
	{
		this.httpSession = httpSession;
	}

	public boolean isResetNeeded()
	{
		return isResetNeeded;
	}

	public void setResetNeeded(final boolean resetNeeded)
	{
		isResetNeeded = resetNeeded;
	}
}
