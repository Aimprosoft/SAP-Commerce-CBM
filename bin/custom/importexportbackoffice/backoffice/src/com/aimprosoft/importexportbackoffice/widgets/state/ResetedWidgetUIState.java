package com.aimprosoft.importexportbackoffice.widgets.state;


import java.util.List;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelList;

import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


public class ResetedWidgetUIState extends AbstractWidgetUIState implements WidgetUIState
{

	private static final String CONNECT_STORAGE_CONFIG_BUTTON = "connectStorageConfigButton";
	private static final String DROP_BOX_AUTH_BUTTON = "dropBoxAuthButton";

	public ResetedWidgetUIState(final WidgetUIContext widgetUIContext)
	{
		super(widgetUIContext);
	}

	@Override
	public void renderDropBoxConnectButton(final String authURL)
	{
		getWidgetUIContext().getButton(CONNECT_STORAGE_CONFIG_BUTTON).setVisible(false);
		getWidgetUIContext().getButton(DROP_BOX_AUTH_BUTTON).setVisible(true);
		getWidgetUIContext().getButton(DROP_BOX_AUTH_BUTTON).setHref(authURL);

		enableActions();

		getWidgetUIContext().changeWidgetUIState(new DropboxAuthorizedWidgetUIState(getWidgetUIContext()));
	}

	@Override
	public void renderConnectAndActionsButtons()
	{
		getWidgetUIContext().getButton(CONNECT_STORAGE_CONFIG_BUTTON).setVisible(true);
		getWidgetUIContext().getButton(CONNECT_STORAGE_CONFIG_BUTTON).setDisabled(false);
		getWidgetUIContext().getButton(DROP_BOX_AUTH_BUTTON).setVisible(false);
		final WidgetInstanceManager widgetInstanceManager = getWidgetUIContext().getWidgetInstanceManager();
		getWidgetUIContext().getButton(CONNECT_STORAGE_CONFIG_BUTTON)
				.setLabel(widgetInstanceManager.getLabel("storageconfig.button.connect"));

		enableActions();

		getWidgetUIContext().changeWidgetUIState(new ReadyToConnectWidgetUIState(getWidgetUIContext()));
	}

	@Override
	public void renderConfigComboBox(final StorageConfigFacade storageConfigFacade, final StorageTypeData storageTypeData)
	{
		final Combobox storageConfigComboBox = (Combobox) getWidgetUIContext().getElement("storageConfigComboBox");

		getWidgetUIContext().getComponent("storageConfigLayout").setVisible(true);

		storageConfigComboBox.setVisible(true);
		final List<StorageConfigData> storageConfigList = storageConfigFacade
				.getStorageConfigsDataByTypeCode(storageTypeData.getCode());
		storageConfigComboBox.setModel(new ListModelList<>(storageConfigList));
		storageConfigComboBox.setFocus(true);
	}

	@Override
	public void resetStorageConfigLayout()
	{
		resetConfigLayout();
	}

	@Override
	public void renderConnectedButtons()
	{
		setConnectedButtons();
	}
}
