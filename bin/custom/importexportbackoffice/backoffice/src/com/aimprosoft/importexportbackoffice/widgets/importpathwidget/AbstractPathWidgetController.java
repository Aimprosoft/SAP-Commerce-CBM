package com.aimprosoft.importexportbackoffice.widgets.importpathwidget;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
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
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import com.aimprosoft.importexportcloud.model.ImportTaskInfoModel;
import com.hybris.backoffice.widgets.advancedsearch.AdvancedSearchMode;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.search.data.SortData;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;
import com.hybris.cockpitng.util.DefaultWidgetController;


public abstract class AbstractPathWidgetController extends DefaultWidgetController //NOSONAR
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractPathWidgetController.class);
	private static final String EXPORT_VIEW_SELECTION = "iemExportBorderLayoutView";
	private static final String IMPORT_VIEW_SELECTION = "iemImportBorderLayoutView";
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

	@SocketEvent(socketId = "selectedView")
	public void loadDataBySelectedView(final Set<String> selectedView)
	{
		if (CollectionUtils.isEmpty(selectedView) || selectedView.contains(IMPORT_VIEW_SELECTION))
		{
			sendExportInfoTasksHistoryEvent(ImportTaskInfoModel._TYPECODE);
		}
		else if (selectedView.contains(EXPORT_VIEW_SELECTION))
		{
			sendExportInfoTasksHistoryEvent(ExportTaskInfoModel._TYPECODE);
		}
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
		try (final InputStream in = new ByteArrayInputStream(source.getByteData()))
		{
			final Path target = Files.createTempFile(source.getName(), null);
			Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
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

	protected void sendExportInfoTasksHistoryEvent(final String typeCode)
	{
		final AdvancedSearchData searchData = new AdvancedSearchData();
		searchData.setSortData(new SortData("code", false));
		searchData.setAdvancedSearchMode(AdvancedSearchMode.SIMPLE);
		searchData.setGlobalOperator(ValueComparisonOperator.OR);
		searchData.setIncludeSubtypes(true);
		searchData.setSearchQueryText("");
		searchData.setSelectedFacets(new HashMap<>());
		searchData.setTypeCode(typeCode);
		sendOutput("outputSearchData", searchData);
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
		final boolean isLocal = storageConfigData != null && BooleanUtils.isTrue(storageConfigData.getStorageTypeData().getIsLocal());
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

	private void setVisibilityValueForLabels(final Boolean isLocal)
	{
		selectedStorageConfigLabel.setVisible(!isLocal);
		accountLabel.setVisible(!isLocal);

		final boolean isExport = getWidgetSettings().getBoolean(IS_EXPORT);
		displayPathLabel.setVisible(!isExport || !isLocal);
		pathLabel.setVisible(!isExport || !isLocal);
	}
}
