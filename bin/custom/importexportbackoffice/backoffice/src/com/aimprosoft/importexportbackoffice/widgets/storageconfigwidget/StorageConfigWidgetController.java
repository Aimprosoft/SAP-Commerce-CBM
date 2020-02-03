package com.aimprosoft.importexportbackoffice.widgets.storageconfigwidget;

import de.hybris.platform.servicelayer.session.SessionService;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Vlayout;

import com.aimprosoft.importexportbackoffice.widgets.handlers.WidgetHandler;
import com.aimprosoft.importexportbackoffice.widgets.state.WidgetUIContext;
import com.aimprosoft.importexportcloud.facades.CloudStorageFacade;
import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.StorageTypeFacade;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;
import com.aimprosoft.importexportcloud.model.StorageConfigModel;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.util.DefaultWidgetController;


public class StorageConfigWidgetController extends DefaultWidgetController //NOSONAR
{
	private static final String SELECTED_TYPE_INDEX = "selectedTypeIndex";

	private Combobox storageTypeComboBox;
	private Combobox storageConfigComboBox;
	private Vlayout storageConfigLayout;
	private Button connectStorageConfigButton;
	private Button editStorageConfigButton;
	private Button removeStorageConfigButton;
	private Button dropBoxAuthButton;
	private Button disconnectButton;

	private WidgetUIContext widgetUIContext;

	@WireVariable
	private transient WidgetHelper widgetHelper;

	@WireVariable
	private transient StorageTypeFacade storageTypeFacade;

	@WireVariable
	private transient StorageConfigFacade storageConfigFacade;

	@WireVariable
	private transient NotificationService notificationService;

	@WireVariable
	private transient CloudStorageFacade cloudStorageFacade;

	@WireVariable
	private transient SessionService sessionService;

	@Override
	public void initialize(final Component comp)
	{
		super.initialize(comp);

		final List<StorageTypeData> storageTypesData = storageTypeFacade.getStorageTypesData();
		storageTypeComboBox.setModel(new ListModelList<>(storageTypesData));

		widgetUIContext = new WidgetUIContext(getWidgetInstanceManager());
		widgetUIContext.addButton("connectStorageConfigButton", connectStorageConfigButton);
		widgetUIContext.addButton("editStorageConfigButton", editStorageConfigButton);
		widgetUIContext.addButton("removeStorageConfigButton", removeStorageConfigButton);
		widgetUIContext.addButton("dropBoxAuthButton", dropBoxAuthButton);
		widgetUIContext.addButton("disconnectButton", disconnectButton);
		widgetUIContext.addElement("storageConfigComboBox", storageConfigComboBox);
		widgetUIContext.addComponent("storageConfigLayout", storageConfigLayout);
	}

	@SocketEvent(socketId = "objectSaved")
	public void onObjectSaved(final Object inputModel)
	{
		if (inputModel instanceof StorageConfigModel)
		{
			final Comboitem comboItem = widgetHelper.getComboitemToSelect(inputModel, storageConfigComboBox);

			widgetHelper.setSelectedConfig(comboItem.getValue(), getWidgetInstanceManager());

			onStorageTypeSelect();
		}
	}

	@ViewEvent(eventName = "onAfterRender", componentID = "storageTypeComboBox")
	public void onStorageTypeComboBoxAfterRender()
	{
		final Integer selectedTypeIndex = getSelectedTypeIndex();

		if (selectedTypeIndex != null)
		{
			storageTypeComboBox.setSelectedIndex(selectedTypeIndex);

			handleTypeSelect(Boolean.FALSE);
		}
		else
		{
			setLocalStorageTypeSelected();
		}
	}

	@ViewEvent(eventName = "onAfterRender", componentID = "storageConfigComboBox")
	public void onStorageConfigComboBoxAfterRender()
	{
		final StorageConfigData selectedConfig = widgetHelper.getSelectedConfig(getWidgetInstanceManager());
		final Integer selectedTypeIndex = getSelectedTypeIndex();

		if (selectedConfig == null || selectedTypeIndex == null)
		{
			return;
		}

		final String codeInStorageType = storageTypeComboBox.getItemAtIndex(selectedTypeIndex).<StorageTypeData>getValue().getCode();
		final String codeInStorageConfig = selectedConfig.getStorageTypeData().getCode();

		if (codeInStorageType.equals(codeInStorageConfig))
		{
			final Optional<Comboitem> selectedItem = storageConfigComboBox.getItems()
					.stream().filter(item -> item.<StorageConfigData>getValue().getCode().equals(selectedConfig.getCode()))
					.findFirst();

			if (selectedItem.isPresent())
			{
				storageConfigComboBox.setSelectedItem(selectedItem.get());

				handleConfigSelect(Boolean.FALSE);
			}
		}
	}

	@ViewEvent(eventName = Events.ON_SELECT, componentID = "storageTypeComboBox")
	public void onStorageTypeSelect()
	{
		handleTypeSelect(Boolean.TRUE);
	}

	private void handleTypeSelect(final boolean isResetNeeded)
	{
		setSelectedTypeIndex(storageTypeComboBox.getSelectedIndex());
		final StorageTypeData storageTypeData = storageTypeComboBox.getSelectedItem().getValue();

		widgetUIContext.setStorageTypeData(storageTypeData);
		widgetUIContext.setResetNeeded(isResetNeeded);

		final WidgetHandler widgetHandler = widgetHelper.getWidgetHandler(HandlerTypeEnum.TYPE_SELECT);

		widgetHandler.handle(widgetUIContext);
	}

	@ViewEvent(eventName = Events.ON_SELECT, componentID = "storageConfigComboBox")
	public void onStorageConfigSelect()
	{
		handleConfigSelect(Boolean.TRUE);
	}

	private void handleConfigSelect(final boolean isResetNeeded)
	{
		final Comboitem selectedItem = storageConfigComboBox.getSelectedItem();
		final StorageConfigData selectedConfigData = selectedItem.getValue();
		final HttpSession nativeSession = (HttpSession) session.getNativeSession();

		widgetUIContext.setResetNeeded(isResetNeeded);
		widgetUIContext.setStorageConfigData(selectedConfigData);
		widgetUIContext.setHttpSession(nativeSession);

		final WidgetHandler widgetHandler = widgetHelper.getWidgetHandler(HandlerTypeEnum.CONFIG_SELECT);

		widgetHandler.handle(widgetUIContext);
	}

	@ViewEvent(eventName = Events.ON_CLICK, componentID = "connectStorageConfigButton")
	public void onConnectButtonClick()
	{
		final StorageConfigData storageConfigData = storageConfigComboBox.getSelectedItem().getValue();

		widgetUIContext.setStorageConfigData(storageConfigData);

		final WidgetHandler widgetHandler = widgetHelper.getWidgetHandler(HandlerTypeEnum.CONNECT);

		widgetHandler.handle(widgetUIContext);

		widgetHelper.closeEditorArea(getWidgetInstanceManager());
	}

	@ViewEvent(eventName = Events.ON_CLICK, componentID = "removeStorageConfigButton")
	public void onRemoveStorageConfigButtonClick()
	{
		Messagebox.show(getLabel("messagebox.storage.config.caution.message"),
				getLabel("messagebox.storage.config.caution.title"),
				Messagebox.YES | Messagebox.CANCEL, Messagebox.QUESTION, e ->
				{
					if (Messagebox.ON_YES.equals(e.getName()))
					{
						removeStorageConfig();
					}
				});
	}

	private void removeStorageConfig()
	{
		final Comboitem selectedItem = storageConfigComboBox.getSelectedItem();

		storageConfigFacade.removeStorageConfig(selectedItem.<StorageConfigData>getValue().getCode());

		storageConfigComboBox.setSelectedItem(null);
		storageConfigComboBox.removeItemAt(selectedItem.getIndex());

		widgetHelper.setSelectedConfig(null, getWidgetInstanceManager());

		getWidgetUIContext().resetStorageConfigLayout();

		widgetHelper.sendResetAndCloseEditorArea(getWidgetInstanceManager());
	}

	@ViewEvent(eventName = Events.ON_CLICK, componentID = "editStorageConfigButton")
	public void onEditStorageConfigButtonClick()
	{
		final String storageConfigCode = storageConfigComboBox.getSelectedItem().<StorageConfigData>getValue().getCode();
		final StorageConfigModel storageConfigModel = storageConfigFacade.getStorageConfigModelByCode(storageConfigCode);

		widgetHelper.openEditorArea(getWidgetInstanceManager(), storageConfigModel);
	}

	@ViewEvent(eventName = Events.ON_CLICK, componentID = "addStorageConfigButton")
	public void onAddStorageConfigButtonClick()
	{
		final String storageTypeCode = storageTypeComboBox.getSelectedItem().<StorageTypeData>getValue().getCode();
		final StorageConfigModel storageConfigModel = storageConfigFacade.createStorageConfigModel(storageTypeCode);

		widgetHelper.openEditorArea(getWidgetInstanceManager(), storageConfigModel);
	}

	@ViewEvent(eventName = Events.ON_CLICK, componentID = "disconnectButton")
	public void onDisconnectButtonClick()
	{
		final Comboitem selectedItem = storageConfigComboBox.getSelectedItem();

		final StorageConfigData storageConfigData = selectedItem != null ? selectedItem.getValue() : null;

		widgetUIContext.setStorageConfigData(storageConfigData);

		final WidgetHandler widgetHandler = widgetHelper.getWidgetHandler(HandlerTypeEnum.DISCONNECT);

		widgetHandler.handle(widgetUIContext);
	}

	private Integer getSelectedTypeIndex()
	{
		return this.getWidgetInstanceManager().getModel().getValue(SELECTED_TYPE_INDEX, Integer.class);
	}

	private void setSelectedTypeIndex(final Integer index)
	{
		this.getWidgetInstanceManager().getModel().put(SELECTED_TYPE_INDEX, index);
	}

	private void setLocalStorageTypeSelected()
	{
		final List<Comboitem> items = storageTypeComboBox.getItems();

		for (final Comboitem item : items)
		{
			if (item.<StorageTypeData>getValue().getIsLocal())
			{
				storageTypeComboBox.setSelectedIndex(item.getIndex());
				onStorageTypeSelect();
			}
		}
	}

	public WidgetUIContext getWidgetUIContext()
	{
		return widgetUIContext;
	}
}
