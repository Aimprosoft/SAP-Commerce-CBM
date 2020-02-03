package com.aimprosoft.importexportbackoffice.widgets.storageconfigwidget;

import com.aimprosoft.importexportbackoffice.widgets.handlers.WidgetHandler;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.model.StorageConfigModel;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;


public interface WidgetHelper
{
	WidgetHandler getWidgetHandler(HandlerTypeEnum typeEnum);

	Comboitem getComboitemToSelect(Object inputModel, Combobox storageConfigComboBox);

	void sendResetAndCloseEditorArea(WidgetInstanceManager widgetInstanceManager);

	void openEditorArea(WidgetInstanceManager widgetInstanceManager, StorageConfigModel storageConfigModel);

	void closeEditorArea(WidgetInstanceManager widgetInstanceManager);

	void setSelectedConfig(final StorageConfigData configData, WidgetInstanceManager widgetInstanceManager);

	StorageConfigData getSelectedConfig(WidgetInstanceManager widgetInstanceManager);

}
