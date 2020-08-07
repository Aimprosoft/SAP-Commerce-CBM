package com.aimprosoft.importexportbackoffice.widgets.importpathwidget;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.exceptions.ImportException;
import com.aimprosoft.importexportcloud.exceptions.TaskException;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Checkbox;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;


public class ImportPathWidgetController extends AbstractPathWidgetController
{
	private static final Logger LOG = Logger.getLogger(ImportPathWidgetController.class);
	private static final String IMPORTSTART = "importstart";
	private static final String IMPORTBLOCKED = "importblocked";
	private static final String REMOVESTART = "removestart";
	private static final String SYNCSTART = "synchronizestart";

	private Checkbox exportMediaNeededCheckBoxImport;

	private Checkbox removeDataCheckBox;

	private Checkbox synchronizeDataCheckBox;

	@Override
	public void initialize(final Component comp)
	{
		super.initialize(comp);
		exportMediaNeededCheckBoxImport.setChecked(getOrCreateTaskInfoData().isExportMediaNeeded());
		removeDataCheckBox.setChecked(getOrCreateTaskInfoData().isRemoveDataNeeded());
		synchronizeDataCheckBox.setChecked(getOrCreateTaskInfoData().isSynchronizeDataNeeded());
		startButton.setLabel(getLabel("task.button.import.start"));
		choosePathButton.setLabel(getLabel("task.button.import.choose"));
		chooseLocalPathButton.setLabel(getLabel("task.button.import.choose"));
		pathLabel.setValue(getLabel("task.label.selected.path"));
	}

	@ViewEvent(eventName = Events.ON_CHECK, componentID = "exportMediaNeededCheckBoxImport")
	public void selectedExportMediaNeededImport()
	{
		getOrCreateTaskInfoData().setExportMediaNeeded(exportMediaNeededCheckBoxImport.isChecked());
	}

	@ViewEvent(eventName = Events.ON_CHECK, componentID = "removeDataCheckBox")
	public void selectedRemoveDataImport()
	{
		getOrCreateTaskInfoData().setRemoveDataNeeded(removeDataCheckBox.isChecked());
	}

	@ViewEvent(eventName = Events.ON_CHECK, componentID = "synchronizeDataCheckBox")
	public void selectedSyncDataImport()
	{
		getOrCreateTaskInfoData().setSynchronizeDataNeeded(synchronizeDataCheckBox.isChecked());
	}

	@ViewEvent(eventName = Events.ON_CLICK, componentID = "choosePathButton")
	public void onChooseImportPathButtonClick(final Event event)
	{
		sendOutput("outputImportConfigData", getOrCreateTaskInfoData().getConfig());
	}

	@ViewEvent(eventName = Events.ON_CLICK, componentID = "startButton")
	public void onStartImportButton()
	{
		try
		{
			cloudStorageFacade.checkActiveTask();
		}
		catch (CloudStorageException e)
		{
			notifyUser(IMPORTBLOCKED, NotificationEvent.Level.FAILURE,  StringUtils.EMPTY, StringUtils.EMPTY);
			return;
		}

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
			taskInfoData.setRemoveDataNeeded(removeDataCheckBox.isChecked());
			taskInfoData.setSynchronizeDataNeeded(synchronizeDataCheckBox.isChecked());
			taskInfoData.setCloudFileDownloadPath(filePath);
			taskInfoData.setCloudFileDownloadPathToDisplay(displayPathLabel.getValue());
			downloadAndImport(taskInfoData);
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

			if (taskInfoData.isRemoveDataNeeded())
			{
				notificationService
						.notifyUser(getWidgetInstanceManager(), REMOVESTART, NotificationEvent.Level.SUCCESS,
								getLabel("import.controller.start.data.remove"));
				cloudStorageFacade.removeOldData(taskInfoData);
			}

			cloudStorageFacade.importData(taskInfoData);

			if (taskInfoData.isSynchronizeDataNeeded())
			{
				notificationService
						.notifyUser(getWidgetInstanceManager(), SYNCSTART, NotificationEvent.Level.SUCCESS,
								getLabel("import.controller.start.data.sync"));
				cloudStorageFacade.synchronizeData(taskInfoData);
			}

			cloudStorageFacade.removeTemporaryFile(taskInfoData);

			notifyUser(IMPORTSTART, NotificationEvent.Level.SUCCESS, getLabel("import.controller.success.file.imported"));
		}
		catch (final CloudStorageException e)
		{
			LOG.error(e.getMessage(), e);
			notifyUser(IMPORTSTART, NotificationEvent.Level.FAILURE, e.getMessage(), getErrorDetails(e.getTaskInfoModel()));
		}
		catch (final ImportException | TaskException e)
		{
			LOG.error(e.getMessage(), e);
			cloudStorageFacade.removeTemporaryFile(taskInfoData);
			notifyUser(IMPORTSTART, NotificationEvent.Level.FAILURE, e.getMessage(), getErrorDetails(e.getTaskInfoModel()));
		}
		setLayoutAfterImporting(taskInfoData.getConfig());
	}
}
