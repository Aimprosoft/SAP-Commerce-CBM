package com.aimprosoft.importexportbackoffice.widgets.cloudpopupwidget;

import static com.aimprosoft.importexportcloud.constants.ImportexportcloudConstants.ROOT_FOLDER;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.CloudStorageFacade;
import com.aimprosoft.importexportcloud.facades.data.CloudObjectData;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.dropbox.core.DbxException;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.util.DefaultWidgetController;


public class CloudFileChooserController extends DefaultWidgetController //NOSONAR
{
	private static final Logger LOGGER = Logger.getLogger(CloudFileChooserController.class);

	@WireVariable
	private transient CloudStorageFacade cloudStorageFacade;

	@Wire
	private Listbox objectList;

	@Wire
	private Button chooseObjectButton;

	@Wire
	private Button backButton;

	private StorageConfigData selectedStorageConfigData;

	private LinkedList<String> historyPathsList;

	private Boolean isExport;

	@SocketEvent(socketId = "inputImportConfig")
	public void onInputImportConfig(final StorageConfigData storageConfigData)
	{
		isExport = Boolean.FALSE;
		handleInputConfig(storageConfigData);
		getWidgetInstanceManager().setTitle(getLabel("widget.popup.import.title"));
	}

	@SocketEvent(socketId = "inputExportConfig")
	public void onInputExportConfig(final StorageConfigData storageConfigData)
	{
		isExport = Boolean.TRUE;
		handleInputConfig(storageConfigData);
		getWidgetInstanceManager().setTitle(getLabel("widget.popup.export.title"));
	}

	@ViewEvent(componentID = "backButton", eventName = "onClick")
	public void onBack()
	{
		final String path = getPreviousFolderPath();
		renderList(selectedStorageConfigData, path);
	}

	@ViewEvent(eventName = Events.ON_CREATE, componentID = "cloudpopupwidget")
	public void onCreatePopup()
	{
		if (CollectionUtils.isEmpty(historyPathsList))
		{
			sendOutput("close", null);
		}
	}

	@ViewEvent(componentID = "chooseObjectButton", eventName = "onClick")
	public void onChooseObject()
	{
		if (objectList.getSelectedItem() == null)
		{
			resolveChooseButtonDisabling(new CloudObjectData());
		}
		else
		{
			final CloudObjectData selectedItem = objectList.getSelectedItem().getValue();
			final String socketName = isExport ? "chosenExportObject" : "chosenImportObject";
			logSelectedPath(selectedItem, socketName);
			sendOutputData(selectedItem, socketName);
		}
	}

	@ViewEvent(componentID = "discard", eventName = "onClick")
	public void onClosePopup()
	{
		sendOutput("close", null);
	}

	protected void addListeners()
	{
		final List<Listitem> items = objectList.getItems();
		for (final Listitem listitem : items)
		{
			final CloudObjectData item = listitem.getValue();
			listitem.addEventListener(Events.ON_CLICK, event -> {
				final CloudObjectData cloudObjectData = ((Listitem) event.getTarget()).getValue();
				resolveChooseButtonDisabling(cloudObjectData);
			});
			if (item.isFolder() && !item.getTitle().equals(ROOT_FOLDER))
			{
				listitem.getChildren().get(0).addEventListener(Events.ON_DOUBLE_CLICK, event -> {
					final Listitem parent = (Listitem) event.getTarget().getParent();
					final CloudObjectData cloudObjectData = parent.getValue();
					resolveChooseButtonDisabling(cloudObjectData);
					renderList(selectedStorageConfigData, cloudObjectData.getName());
					backButton.setDisabled(Boolean.FALSE);
					historyPathsList.addLast(cloudObjectData.getName());
				});
			}
		}
	}

	private void sendOutputData(final CloudObjectData selectedItem, final String socket)
	{
		final HashMap<String, String> data = new HashMap<>();
		data.put("name", selectedItem.getName());
		data.put("title", selectedItem.getTitle());
		data.put("pathDisplay", selectedItem.getPathDisplay());
		sendOutput(socket, data);
	}

	private void resolveChooseButtonDisabling(final CloudObjectData cloudObjectData)
	{
		boolean isDisabled = Boolean.TRUE;

		if (!cloudObjectData.isFolder())
		{
			isDisabled = StringUtils.isEmpty(cloudObjectData.getName());
		}
		else if (isExport)
		{
			isDisabled = Boolean.FALSE;
		}

		chooseObjectButton.setDisabled(isDisabled);
	}

	private void renderList(final StorageConfigData storageConfigData, final String cloudPath)
	{
		try
		{
			final TaskInfoData taskInfoData = new TaskInfoData();
			taskInfoData.setConfig(storageConfigData);
			taskInfoData.setCloudFolderPath(cloudPath);
			taskInfoData.setIsExport(isExport);

			final Collection<CloudObjectData> cloudObjectData = cloudStorageFacade.listFiles(taskInfoData);
			final ListModelList<CloudObjectData> items = new ListModelList<>(cloudObjectData);

			resolveRootFolder(cloudPath, items);

			objectList.setModel(items);

			if (objectList.getSelectedItem() == null || CollectionUtils.isEmpty(items))
			{
				resolveChooseButtonDisabling(new CloudObjectData());
			}
			objectList.renderAll();

			addListeners();
		}
		catch (final CloudStorageException | DbxException e)
		{
			LOGGER.error("Error occurred while render popup: ", e);
			Clients.showNotification(getWidgetInstanceManager().getLabel("widget.error.connection"),
					Clients.NOTIFICATION_TYPE_WARNING, objectList, "top_center", 5000, true);
		}
	}

	private void resolveRootFolder(final String cloudPath, final ListModelList<CloudObjectData> items)
	{
		if (StringUtils.isEmpty(cloudPath))
		{
			final CloudObjectData cloudDataForRootFolder = CloudStorageFacade
					.getCloudDataForRootFolder();
			cloudDataForRootFolder.setName(selectedStorageConfigData.getRootFolder());
			items.add(0, cloudDataForRootFolder);
		}
	}

	private void handleInputConfig(final StorageConfigData storageConfigData)
	{
		backButton.setDisabled(Boolean.TRUE);
		historyPathsList = new LinkedList<>();
		historyPathsList.add(StringUtils.EMPTY);
		selectedStorageConfigData = storageConfigData;
		renderList(storageConfigData, StringUtils.EMPTY);
	}

	private String getPreviousFolderPath()
	{
		String path = StringUtils.EMPTY;
		if (CollectionUtils.isNotEmpty(historyPathsList))
		{
			historyPathsList.removeLast();
			path = historyPathsList.getLast();

			if (StringUtils.EMPTY.equals(path))
			{
				backButton.setDisabled(Boolean.TRUE);
			}
		}
		return path;
	}

	private void logSelectedPath(final CloudObjectData selectedItem, final String socket)
	{
		final String title = selectedItem.getTitle();
		final String pathDisplay = selectedItem.getPathDisplay();
		final String itemType = selectedItem.isFolder() ? "folder" : "file";

		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug(String.format("Selected %s '%s' with path '%s'. Socket: '%s'", itemType, title, pathDisplay, socket));
		}
	}
}
