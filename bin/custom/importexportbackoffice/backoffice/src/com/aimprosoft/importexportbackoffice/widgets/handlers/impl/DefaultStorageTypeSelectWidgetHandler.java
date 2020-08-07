package com.aimprosoft.importexportbackoffice.widgets.handlers.impl;

import static com.aimprosoft.importexportbackoffice.widgets.storageconfigwidget.DefaultWidgetHelper.OUTPUT_SOCKET_OUTPUT_STORAGE_CONFIG_DATA;
import static com.aimprosoft.importexportbackoffice.widgets.storageconfigwidget.DefaultWidgetHelper.OUTPUT_SOCKET_OUTPUT_STORAGE_TYPE;
import static com.aimprosoft.importexportcloud.constants.ImportexportcloudConstants.LOCAL_STORAGE_CONFIG;

import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportbackoffice.widgets.handlers.WidgetHandler;
import com.aimprosoft.importexportbackoffice.widgets.state.WidgetUIContext;
import com.aimprosoft.importexportbackoffice.widgets.storageconfigwidget.WidgetHelper;
import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


public class DefaultStorageTypeSelectWidgetHandler implements WidgetHandler
{
	private WidgetHelper widgetHelper;

	private StorageConfigFacade storageConfigFacade;

	@Override
	public void handle(final WidgetUIContext widgetUIContext)
	{
		widgetUIContext.resetStorageConfigLayout();

		final WidgetInstanceManager widgetInstanceManager = widgetUIContext.getWidgetInstanceManager();
		final StorageTypeData storageTypeData = widgetUIContext.getStorageTypeData();
		final boolean isResetNeeded = widgetUIContext.isResetNeeded();

		if (isResetNeeded)
		{
			widgetHelper.sendResetAndCloseEditorArea(widgetInstanceManager);
		}

		if (storageTypeData.getIsLocal())
		{
			handleLocalStorageType(widgetInstanceManager, widgetUIContext);
		}
		else
		{
			widgetUIContext.renderConfigComboBox(storageConfigFacade, storageTypeData);
		}

		widgetInstanceManager.sendOutput(OUTPUT_SOCKET_OUTPUT_STORAGE_TYPE, storageTypeData);
	}

	private void handleLocalStorageType(final WidgetInstanceManager widgetInstanceManager, final WidgetUIContext widgetUIContext)
	{
		final StorageConfigData localStorageConfig = storageConfigFacade.getStorageConfigData(LOCAL_STORAGE_CONFIG);

		widgetInstanceManager.sendOutput(OUTPUT_SOCKET_OUTPUT_STORAGE_CONFIG_DATA, localStorageConfig);

		widgetUIContext.getComponent("storageConfigLayout").setVisible(false);
	}

	public WidgetHelper getWidgetHelper()
	{
		return widgetHelper;
	}

	@Required
	public void setWidgetHelper(final WidgetHelper widgetHelper)
	{
		this.widgetHelper = widgetHelper;
	}

	public StorageConfigFacade getStorageConfigFacade()
	{
		return storageConfigFacade;
	}

	@Required
	public void setStorageConfigFacade(final StorageConfigFacade storageConfigFacade)
	{
		this.storageConfigFacade = storageConfigFacade;
	}
}
