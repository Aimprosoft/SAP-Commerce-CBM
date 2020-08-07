package com.aimprosoft.importexportbackoffice.widgets.importpathwidget;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;

import com.aimprosoft.importexportcloud.enums.TaskInfoScope;
import com.aimprosoft.importexportcloud.facades.CloudStorageFacade;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.TaskInfoModel;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.util.DefaultWidgetController;


public abstract class AbstractPathWidgetController extends DefaultWidgetController //NOSONAR
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractPathWidgetController.class);

	private static final String LABEL_NONE_KEY = "task.label.none";
	private static final String CURRENT_TASK_INFO_DATA = "currentTaskInfo";
	private static final String IS_EXPORT = "isExport";

	protected Label displayPathLabel;
	protected Label realFilePathLabel;
	private Label selectedStorageConfigLabel;
	protected Button choosePathButton;
	protected Button chooseLocalPathButton;
	private Label storageName;
	protected Label pathLabel;
	private Label accountLabel;
	protected Button startButton;

	@WireVariable
	protected transient NotificationService notificationService;

	@WireVariable
	protected transient CloudStorageFacade cloudStorageFacade;

	@Override
	public void initialize(final Component comp)
	{
		super.initialize(comp);
		/* create taskInfoData's for both widgets */
		getOrCreateTaskInfoData();
	}

	@SocketEvent(socketId = "reset")
	public void onReset()
	{
		setTaskInfoData(null);
		resetEverythingInCommon();
	}

	@SocketEvent(socketId = "inputStorageConfigData")
	public void onInputStorageConfigData(final StorageConfigData storageConfigData)
	{
		final TaskInfoData taskInfoData = getOrCreateTaskInfoData();
		taskInfoData.setConfig(storageConfigData);

		fillConfigLabels(storageConfigData);
		showAppropriateChoosePathButton(storageConfigData);


		final Boolean isLocal = storageConfigData.getStorageTypeData().getIsLocal();
		setVisibilityValueForLabels(isLocal);
	}

	@SocketEvent(socketId = "inputStorageTypeData")
	public void onInputStorageTypeData(final StorageTypeData storageType)
	{
		storageName.setValue(storageType.getName());
		accountLabel.setVisible(!storageType.getIsLocal());
	}

	@ViewEvent(eventName = Events.ON_UPLOAD, componentID = "chooseLocalPathButton")
	public void onChooseLocalPathButtonClick(final UploadEvent event)
	{
		final Media media = event.getMedia();
		realFilePathLabel.setValue(getMediaPath(media));
		displayPathLabel.setValue(media.getName());
		startButton.setDisabled(false);
	}

	protected void notifyUser(final String event, final NotificationEvent.Level level, final Object... referenceObjects)
	{
		notificationService
				.notifyUser(getWidgetInstanceManager(), event, level, referenceObjects);
	}

	protected TaskInfoData getOrCreateTaskInfoData()
	{
		TaskInfoData currentTaskInfoData = getWidgetInstanceManager().getModel()
				.getValue(CURRENT_TASK_INFO_DATA, TaskInfoData.class);

		if (currentTaskInfoData == null)
		{
			currentTaskInfoData = new TaskInfoData();
			currentTaskInfoData.setTaskInfoScopeCode(TaskInfoScope.SITESCOPE.getCode());
			currentTaskInfoData.setExportMediaNeeded(true);
			setTaskInfoData(currentTaskInfoData);
		}

		return currentTaskInfoData;
	}

	private void setTaskInfoData(final TaskInfoData taskInfoData)
	{
		this.getWidgetInstanceManager().getModel().put(CURRENT_TASK_INFO_DATA, taskInfoData);
	}

	private String getMediaPath(final Media source)
	{
		String path = StringUtils.EMPTY;
		try
		{
			final Path target = Files.createTempFile(source.getName(), null);
			Files.copy(source.getStreamData(), target, StandardCopyOption.REPLACE_EXISTING);
			path = target.toString();
		}
		catch (final IOException e)
		{
			LOG.error("An error occurred while getting local file path: " + e);
			notificationService
					.notifyUser(this.getWidgetInstanceManager(), Events.ON_UPLOAD, NotificationEvent.Level.FAILURE);
		}

		return path;
	}

	private void resetEverythingInCommon()
	{
		clearLabels();
		clearButtons();
	}

	private void clearLabels()
	{
		selectedStorageConfigLabel.setValue(getLabel(LABEL_NONE_KEY));
		clearPathLabels();
	}

	private void clearButtons()
	{
		/* show choosePathButton for storage on reset. It doesn't matter. It will be changed on storage type selection */
		showAppropriateChoosePathButton(getOrCreateTaskInfoData().getConfig());
		startButton.setDisabled(Boolean.TRUE);
	}

	private void showAppropriateChoosePathButton(final StorageConfigData storageConfigData)
	{
		final boolean isLocal =
				storageConfigData != null && BooleanUtils.isTrue(storageConfigData.getStorageTypeData().getIsLocal());
		final boolean isConnected = storageConfigData != null && BooleanUtils.isTrue(storageConfigData.getIsConnected());
		final boolean isExport = getWidgetSettings().getBoolean(IS_EXPORT);

		chooseLocalPathButton.setVisible(isLocal && !isExport);
		choosePathButton.setVisible(!isLocal);
		chooseLocalPathButton.setDisabled(isExport);
		choosePathButton.setDisabled(isExport || !isConnected);
	}

	// in future set another labels
	private void fillConfigLabels(final StorageConfigData storageConfigData)
	{
		selectedStorageConfigLabel.setValue(storageConfigData.getName());
	}

	protected void clearPathLabels()
	{
		realFilePathLabel.setValue(StringUtils.EMPTY);
		displayPathLabel.setValue(StringUtils.EMPTY);
	}

	protected void fillPathLabels(final Map<String, String> inputMap)
	{
		realFilePathLabel.setValue(inputMap.get("name"));
		displayPathLabel.setValue(inputMap.get("pathDisplay"));
	}

	protected Object getErrorDetails(final TaskInfoModel taskInfo)
	{
		return taskInfo == null ? StringUtils.EMPTY : taskInfo;
	}

	private void setVisibilityValueForLabels(final Boolean isLocal)
	{
		selectedStorageConfigLabel.setVisible(!isLocal);
		accountLabel.setVisible(!isLocal);

		final boolean isExport = getWidgetSettings().getBoolean(IS_EXPORT);
		displayPathLabel.setVisible(!isExport || !isLocal);
		pathLabel.setVisible(!isExport || !isLocal);
	}
}
