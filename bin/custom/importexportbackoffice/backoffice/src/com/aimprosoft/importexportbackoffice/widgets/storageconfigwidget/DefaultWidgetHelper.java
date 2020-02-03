package com.aimprosoft.importexportbackoffice.widgets.storageconfigwidget;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;

import com.aimprosoft.importexportbackoffice.widgets.handlers.WidgetHandler;
import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.model.StorageConfigModel;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


public class DefaultWidgetHelper implements WidgetHelper
{
	private static final String VIRTUAL_OUTPUT_SOCKET_CLOSE_EAST = "closeEast";
	private static final String VIRTUAL_OUTPUT_SOCKET_OPEN_EAST = "openEast";
	private static final String OUTPUT_SOCKET_OUTPUT_OBJECT = "outputObject";
	private static final String SELECTED_CONFIG = "selectedConfig";
	private static final String OUTPUT_SOCKET_RESET = "reset";
	public static final String OUTPUT_SOCKET_OUTPUT_STORAGE_CONFIG_DATA = "outputStorageConfigData";
	public static final String OUTPUT_SOCKET_OUTPUT_STORAGE_TYPE = "outputStorageTypeData";
	public static final String CONNECTION_STATUS = "connectionStatus";
	public static final String DISCONNECT = "disconnectStatus";

	private StorageConfigFacade storageConfigFacade;

	private Map<String, WidgetHandler> widgetHandlerMap;

	@Override
	public WidgetHandler getWidgetHandler(final HandlerTypeEnum typeEnum)
	{
		return widgetHandlerMap.get(typeEnum.getTypeCode());
	}

	@Override
	public Comboitem getComboitemToSelect(final Object inputModel, final Combobox storageConfigComboBox)
	{
		final StorageConfigModel storageConfigModel = (StorageConfigModel) inputModel;
		final StorageConfigData storageConfigData = storageConfigFacade
				.getStorageConfigData(storageConfigModel.getCode());

		final Optional<Comboitem> first = storageConfigComboBox.getItems().stream()
				.filter(item -> item.<StorageConfigData>getValue().getCode().equals(storageConfigModel.getCode()))
				.findFirst();

		return first.orElseGet(() -> {
			final Comboitem result = storageConfigComboBox.appendItem(storageConfigData.getName());
			result.setValue(storageConfigData);
			return result;
		});
	}

	@Override
	public void sendResetAndCloseEditorArea(final WidgetInstanceManager widgetInstanceManager)
	{
		widgetInstanceManager.sendOutput(OUTPUT_SOCKET_RESET, null);
		widgetInstanceManager.sendOutput(VIRTUAL_OUTPUT_SOCKET_CLOSE_EAST, null);
	}

	@Override
	public void openEditorArea(final WidgetInstanceManager widgetInstanceManager, final StorageConfigModel storageConfigModel)
	{
		widgetInstanceManager.sendOutput(VIRTUAL_OUTPUT_SOCKET_OPEN_EAST, null);
		widgetInstanceManager.sendOutput(OUTPUT_SOCKET_OUTPUT_OBJECT, storageConfigModel);
	}

	@Override
	public void closeEditorArea(final WidgetInstanceManager widgetInstanceManager)
	{
		widgetInstanceManager.sendOutput(VIRTUAL_OUTPUT_SOCKET_CLOSE_EAST, null);
	}

	@Override
	public void setSelectedConfig(final StorageConfigData configData, final WidgetInstanceManager widgetInstanceManager)
	{
		widgetInstanceManager.getModel().put(SELECTED_CONFIG, configData);
	}

	@Override
	public StorageConfigData getSelectedConfig(final WidgetInstanceManager widgetInstanceManager)
	{
		return widgetInstanceManager.getModel().getValue(SELECTED_CONFIG, StorageConfigData.class);
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

	@Required
	public void setWidgetHandlerMap(final Map<String, WidgetHandler> widgetHandlerMap)
	{
		this.widgetHandlerMap = widgetHandlerMap;
	}
}
