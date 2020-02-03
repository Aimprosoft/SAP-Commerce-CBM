package com.aimprosoft.importexportbackoffice.widgets.importpathwidget;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Checkbox;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.exceptions.ImportException;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.ImportTaskInfoModel;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;


public class ImportPathWidgetController extends AbstractPathWidgetController
{
	private static final Logger LOG = Logger.getLogger(ImportPathWidgetController.class);
	private static final String IMPORTSTART = "importstart";

	private Checkbox exportMediaNeededCheckBoxImport;

	@Override
	public void initialize(final Component comp)
	{
		super.initialize(comp);
		exportMediaNeededCheckBoxImport.setChecked(getOrCreateTaskInfoData().isExportMediaNeeded());

		startButton.setLabel(getLabel("task.button.import.start"));
		choosePathButton.setLabel(getLabel("task.button.import.choose"));
		chooseLocalPathButton.setLabel(getLabel("task.button.import.choose"));
		pathLabel.setValue(getLabel("task.label.selected.path"));
	}

	@ViewEvent(componentID = "exportMediaNeededCheckBoxImport", eventName = "onSelectedExportMediaNeededImport")
	public void selectedExportMediaNeededImport()
	{
		getOrCreateTaskInfoData().setExportMediaNeeded(exportMediaNeededCheckBoxImport.isChecked());
	}

	@ViewEvent(eventName = Events.ON_CLICK, componentID = "choosePathButton")
	public void onChooseImportPathButtonClick(final Event event)
	{
		sendOutput("outputImportConfigData", getOrCreateTaskInfoData().getConfig());
	}

	@ViewEvent(eventName = Events.ON_CLICK, componentID = "startButton")
	public void onStartImportButton()
	{
		String filePath = realFilePathLabel.getValue();
		try
		{
			filePath = URLDecoder.decode(realFilePathLabel.getValue(), "UTF-8");
		}
		catch (final UnsupportedEncodingException e)
		{
			LOG.error(e.getMessage(), e);
			notifyUser(IMPORTSTART, NotificationEvent.Level.FAILURE, getLabel("import.controller.failure.file.decode"));
		}

		if (StringUtils.isBlank(filePath))
		{
			notifyUser(IMPORTSTART, NotificationEvent.Level.FAILURE, getLabel("import.controller.failure.file.path.blank"));
		}
		else
		{
			final TaskInfoData taskInfoData = getOrCreateTaskInfoData();

			taskInfoData.setExportMediaNeeded(exportMediaNeededCheckBoxImport.isChecked());

			taskInfoData.setCloudFileDownloadPath(filePath);
			taskInfoData.setCloudFileDownloadPathToDisplay(displayPathLabel.getValue());
			downloadAndImport(taskInfoData);

			sendExportInfoTasksHistoryEvent(ImportTaskInfoModel._TYPECODE);
		}
	}

	@SocketEvent(socketId = "inputChoosenObject")
	public void onChosenObject(final Map<String, String> inputMap)
	{
		fillPathLabels(inputMap);
		startButton.setDisabled(false);
	}

	private void setLayoutAfterImporting(final StorageConfigData storageConfigData)
	{
		onReset();
		onInputStorageConfigData(storageConfigData);
	}

	private void downloadAndImport(final TaskInfoData taskInfoData)
	{
		try
		{
			cloudStorageFacade.download(taskInfoData);
			notifyUser(IMPORTSTART, NotificationEvent.Level.SUCCESS, getLabel("import.controller.success.file.downloaded"));

			cloudStorageFacade.importData(taskInfoData);
			notifyUser(IMPORTSTART, NotificationEvent.Level.SUCCESS, getLabel("import.controller.success.file.imported"));
		}
		catch (final CloudStorageException e)
		{
			LOG.error(e.getMessage(), e);
			notifyUser(IMPORTSTART, NotificationEvent.Level.FAILURE, e.getMessage());
		}
		catch (final ImportException e)
		{
			LOG.error(e.getMessage(), e);
			cloudStorageFacade.removeTemporaryFile(taskInfoData);
			notifyUser(IMPORTSTART, NotificationEvent.Level.FAILURE, e.getMessage(), e.getTaskInfoModel());
		}

		setLayoutAfterImporting(taskInfoData.getConfig());
	}
}
