package com.aimprosoft.importexportbackoffice.widgets.state;

import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;


public interface WidgetUIState
{
	void renderDropBoxConnectButton(String authURL);

	void renderConnectAndActionsButtons();

	void resetStorageConfigLayout();

	void renderConnectedButtons();

	void renderConfigComboBox(StorageConfigFacade storageConfigFacade, StorageTypeData storageTypeData);
}
