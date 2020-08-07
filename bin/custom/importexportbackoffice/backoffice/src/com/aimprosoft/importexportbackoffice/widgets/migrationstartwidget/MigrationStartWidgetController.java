package com.aimprosoft.importexportbackoffice.widgets.migrationstartwidget;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;

import com.aimprosoft.importexportbackoffice.widgets.controllers.AbstractIemWidgetController;
import com.aimprosoft.importexportcloud.enums.TaskInfoStatus;
import com.aimprosoft.importexportcloud.event.MigrationTaskEvent;
import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.exceptions.MigrationException;
import com.aimprosoft.importexportcloud.facades.CloudStorageFacade;
import com.aimprosoft.importexportcloud.facades.MediaFolderFacade;
import com.aimprosoft.importexportcloud.facades.data.MediaFolderData;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.MigrationTaskInfoModel;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.annotations.GlobalCockpitEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.core.events.CockpitEvent;


public class MigrationStartWidgetController extends AbstractIemWidgetController //NOSONAR
{
	private static final Logger LOG = LoggerFactory.getLogger(MigrationStartWidgetController.class);
	private static final String EVENT_TYPE = "migrationstart";
	private static final String CURRENT_TASK_INFO_DATA = "currentTaskInfo";
	private static final String MIGRATION_WIDGET_SOURCE_PATH = "migration.widget.source.path";
	private static final String SELECTED_FOLDER_DATA = "selectedFolderData";
	private static final String MIGRATION_WIDGET_TARGET_PATH = "migration.widget.target.path";
	private static final String MIGRATION_WIDGET_LOCAL_PATH = "migration.widget.local.path";
	private static final String EMPTY_CONFIG_VALUE_LABEL = "media.folder.none.config";
	private static final String WARNING_LABEL_MESSAGE_CLASS = "warning-label-message";
	private static final String PRIMARY_WIDGET_LABEL = "primary-widget-label";
	private static final String INCOMPLETE_MIGRATION_LABEL = "migration.widget.incomplete.migration";
	private static final String MIGRATION_NOT_FINISHED_YET = "migration.widget.previous.migration.not.finished";
	private static final String MIGRATION_WIDGET_ACCOUNT = "migration.widget.account";

	@WireVariable
	private CloudStorageFacade cloudStorageFacade;
	@WireVariable
	private NotificationService notificationService;
	@WireVariable
	private MediaFolderFacade mediaFolderFacade;
	@WireVariable
	private MediaService mediaService;
	@WireVariable
	private UserService userService;
	@WireVariable
	private StorageConfigFacade storageConfigFacade;

	private Combobox mediaFoldersComboBox;
	private Button startMigrationButton;
	private Label sourceStorageLabel;
	private Label targetStorageLabel;
	private Label incompleteMigrationLabel;
	private Label matchStoragesWarningLabel;

	@Override
	public void initialize(final Component comp)
	{
		super.initialize(comp);
		incompleteMigrationLabel.setVisible(false);
		startMigrationButton.setDisabled(true);
		final ListModelList<MediaFolderData> listModelList = getAllFolders();
		mediaFoldersComboBox.setModel(listModelList);

		final MediaFolderData selectedFolderData = getWidgetInstanceManager().getModel()
				.getValue(SELECTED_FOLDER_DATA, MediaFolderData.class);

		setNotSelectedSourceAndTargetLabels();
		resolveSourceStorageLabelValue(selectedFolderData);
	}

	@SocketEvent(socketId = "inputStorageConfigData")
	public void onInputStorageConfigData(final StorageConfigData storageConfigData)
	{
		final TaskInfoData taskInfoData = getOrCreateTaskInfoData();
		taskInfoData.setConfig(storageConfigData);
		updateTaskInfoDataForWidgetModel(taskInfoData);
		resolveTargetStorageLabelValue(taskInfoData);

		if (isBlockedForMigration(taskInfoData))
		{
			handleIncompleteLabel(Boolean.TRUE);
			resetUIComponents();
		}

		if (StringUtils.isEmpty(taskInfoData.getMediaFolderQualifier()))
		{
			resetFoldersCombobox();
			setNotSelectedSourceAndTargetLabels();
		}
		resolveStartButtonVisibility();
	}

	@ViewEvent(eventName = Events.ON_CLICK, componentID = "startMigrationButton")
	public void onStartMigrationButton()
	{
		if (!isMigrationConfigValid())
		{
			notifyUser(NotificationEvent.Level.WARNING, getLabel("task.migration.notification.invalid"));
		}
		else
		{
			try
			{
				cloudStorageFacade.checkActiveTask();
			}
			catch (CloudStorageException e)
			{
				notifyUser(NotificationEvent.Level.FAILURE, getLabel("migration.widget.check.task.info"));
				return;
			}

			showConfirmationModalAndMigrate();
		}
	}

	@ViewEvent(eventName = Events.ON_SELECT, componentID = "mediaFoldersComboBox")
	public void onSelectedFolder()
	{
		final MediaFolderData selectedFolderData = ofNullable(mediaFoldersComboBox.getSelectedItem())
				.map(Comboitem::<MediaFolderData>getValue)
				.orElse(null);

		final String qualifier = ofNullable(selectedFolderData)
				.map(MediaFolderData::getQualifier)
				.orElse(StringUtils.EMPTY);
		final TaskInfoData taskInfoData = getOrCreateTaskInfoData();
		taskInfoData.setMediaFolderQualifier(qualifier);

		updateTaskInfoDataForWidgetModel(taskInfoData);

		setValueToWidgetModel(SELECTED_FOLDER_DATA, selectedFolderData);
		resolveSourceStorageLabelValue(selectedFolderData);

		final boolean isBlocked = isBlockedForMigration(taskInfoData);
		handleIncompleteLabel(isBlocked);
		handleFinishedCompletelyLastMigration(taskInfoData);
		resolveStartButtonVisibility();

		resolveTargetStorageLabelValue(taskInfoData);

		final String storageConfigCode = selectedFolderData.getStorageConfigCode();
		taskInfoData.setSourceConfigCode(storageConfigCode);
	}

	private boolean isBlockedForMigration(final TaskInfoData taskInfoData)
	{
		MediaFolderModel mediaFolder;
		boolean isBlockedForMigration = false;
		if (StringUtils.isNotEmpty(taskInfoData.getMediaFolderQualifier()))
		{
			mediaFolder = mediaService.getFolder(taskInfoData.getMediaFolderQualifier());
			isBlockedForMigration = mediaFolder.isBlockedForMigration();
		}
		return isBlockedForMigration;
	}

	@ViewEvent(eventName = "onAfterRender", componentID = "mediaFoldersComboBox")
	public void onAfterRenderFolderCombobox()
	{
		final TaskInfoData taskInfoData = getOrCreateTaskInfoData();
		mediaFoldersComboBox.getItems().stream()
				.filter(item -> item.<MediaFolderData>getValue().getQualifier().equals(taskInfoData.getMediaFolderQualifier()))
				.findFirst()
				.ifPresent(mediaFoldersComboBox::setSelectedItem);

		if (StringUtils.isNotEmpty(taskInfoData.getMediaFolderQualifier()))
		{
			handleFinishedCompletelyLastMigration(taskInfoData);
		}
	}

	@SocketEvent(socketId = "reset")
	public void onReset()
	{
		resetItemsValues();
		resolveStartButtonVisibility();
	}

	@GlobalCockpitEvent(eventName = "com.aimprosoft.importexportcloud.event.MigrationTaskEvent", scope = CockpitEvent.APPLICATION)
	public void onMigrationCronJobFinished(final CockpitEvent cockpitEvent)
	{
		final MigrationTaskEvent taskEvent = (MigrationTaskEvent) cockpitEvent.getData();
		final MigrationTaskInfoModel migrationTask = taskEvent.getMigrationTask();
		final TaskInfoStatus status = migrationTask.getStatus();
		final NotificationEvent.Level level = TaskInfoStatus.COMPLETED.equals(status)
				? NotificationEvent.Level.SUCCESS
				: (TaskInfoStatus.ABORTED.equals(status)
				? NotificationEvent.Level.WARNING
				: NotificationEvent.Level.FAILURE);
		if (userService.getCurrentUser().getUid().equals(taskEvent.getUser().getUid()))
		{
			notifyUser(level, getLabel("task.migration.notification.finished",
					new Object[] { migrationTask.getStorageConfig().getName() }));
		}
		resetFoldersCombobox();
	}

	private void showConfirmationModalAndMigrate()
	{
		final TaskInfoData taskInfoData = getOrCreateTaskInfoData();
		final MediaFolderData selectedFolderData = getValueFromWidgetModel(SELECTED_FOLDER_DATA, MediaFolderData.class);
		final String confirmationMessage = getLabel("migration.widget.confirmation.message.main");
		final String sourceMessage = formatLabels("migration.widget.confirmation.message.source", selectedFolderData.getQualifier(),
				selectedFolderData.getStorageTypeName(), selectedFolderData.getStorageConfigName());
		final String targetMessage = formatLabels("migration.widget.confirmation.message.target",
				taskInfoData.getConfig().getStorageTypeData().getName(), taskInfoData.getConfig().getName());

		Messagebox.show(String.format("%s %n %s %n %s", confirmationMessage, sourceMessage, targetMessage),
				getLabel("migration.widget.confirmation.title"),
				Messagebox.YES | Messagebox.CANCEL, Messagebox.QUESTION, e ->
				{
					if (Messagebox.ON_YES.equals(e.getName()))
					{
						incompleteMigrationLabel.setVisible(false);
						migrateMedia();
						taskInfoData.setMediaFolderQualifier(StringUtils.EMPTY);
					}
				});
	}

	private void handleIncompleteLabel(final boolean isBlocked)
	{
		if (isBlocked)
		{
			incompleteMigrationLabel.setValue(getLabel(MIGRATION_NOT_FINISHED_YET));
			incompleteMigrationLabel.setVisible(true);
		}
		else
		{
			incompleteMigrationLabel.setVisible(false);
		}
	}

	private void handleFinishedCompletelyLastMigration(final TaskInfoData taskInfoData)
	{
		final MediaFolderModel mediaFolder = mediaService.getFolder(taskInfoData.getMediaFolderQualifier());
		final MigrationTaskInfoModel migrationTaskInfo = mediaFolder.getLastMigrationTaskInfo();
		if (migrationTaskInfo != null)
		{
			final TaskInfoStatus status = migrationTaskInfo.getStatus();
			if (TaskInfoStatus.PARTLYMIGRATED.equals(status))
			{
				incompleteMigrationLabel.setVisible(true);
				incompleteMigrationLabel.setValue(getLabel(INCOMPLETE_MIGRATION_LABEL));
			}
		}
	}

	private void migrateMedia()
	{
		try
		{
			final TaskInfoData taskInfoData = getOrCreateTaskInfoData();
			cloudStorageFacade.migrateMediaViaCronJob(taskInfoData);
			notifyUser(NotificationEvent.Level.SUCCESS, getLabel("task.migration.notification.started"));
			resetUIComponents();
		}
		catch (final MigrationException e)
		{
			LOG.error(e.getMessage(), e);
			notifyUser(NotificationEvent.Level.FAILURE, e.getMessage());
		}
	}

	private void resetUIComponents()
	{
		startMigrationButton.setDisabled(true);
		mediaFoldersComboBox.setSelectedItem(null);
		resetFoldersCombobox();
		setNotSelectedSourceAndTargetLabels();
		updateTaskInfoDataForWidgetModel(getOrCreateTaskInfoData());
	}

	private void resolveStartButtonVisibility()
	{
		final boolean isStartAllowed = isMigrationConfigValid();
		startMigrationButton.setDisabled(!isStartAllowed);
	}

	private boolean isSourceTargetConfigsValid(final TaskInfoData taskInfoData, final MediaFolderData selectedFolderData)
	{
		boolean isValid = Boolean.FALSE;
		final String code = taskInfoData.getConfig() == null ? StringUtils.EMPTY : taskInfoData.getConfig().getCode();

		if (selectedFolderData != null)
		{
			isValid = !code.equals(selectedFolderData.getStorageConfigCode());
		}

		highlightLabelsForWarning(isValid);

		return isValid;
	}

	private boolean isMigrationConfigValid()
	{
		final TaskInfoData taskInfoData = getOrCreateTaskInfoData();
		final StorageConfigData config = taskInfoData.getConfig();
		final String mediaFolderQualifier = taskInfoData.getMediaFolderQualifier();
		Boolean isConnected = Boolean.FALSE;
		if (config != null)
		{
			isConnected = config.getStorageTypeData().getIsLocal() ? Boolean.TRUE : config.getIsConnected();
		}
		final MediaFolderData selectedFolder = getValueFromWidgetModel(SELECTED_FOLDER_DATA, MediaFolderData.class);

		return config != null && isTrue(isConnected) && StringUtils.isNotBlank(mediaFolderQualifier)
				&& isSourceTargetConfigsValid(taskInfoData, selectedFolder) && !isBlockedForMigration(taskInfoData);
	}

	private void notifyUser(final NotificationEvent.Level level, final String message)
	{
		notificationService.notifyUser(getWidgetInstanceManager(), EVENT_TYPE, level, message);
	}

	private void resolveTargetStorageLabelValue(final TaskInfoData taskInfoData)
	{
		if (taskInfoData.getConfig() != null)
		{
			if (taskInfoData.getConfig().getStorageTypeData().getIsLocal())
			{
				targetStorageLabel.setValue(formatLabels(MIGRATION_WIDGET_TARGET_PATH, getLabel(MIGRATION_WIDGET_LOCAL_PATH)));
			}
			else
			{
				final String storageName = taskInfoData.getConfig().getName();
				final String storageTypeName = taskInfoData.getConfig().getStorageTypeData().getName();
				targetStorageLabel.setValue(formatLabels(MIGRATION_WIDGET_TARGET_PATH,
						formatLabels(MIGRATION_WIDGET_ACCOUNT, storageTypeName, storageName)));
			}
		}
		else
		{
			targetStorageLabel.setValue(formatLabels(MIGRATION_WIDGET_TARGET_PATH, getLabel(EMPTY_CONFIG_VALUE_LABEL)));
		}
	}

	private void resetItemsValues()
	{
		mediaFoldersComboBox.setSelectedItem(null);
		setValueToWidgetModel(CURRENT_TASK_INFO_DATA, null);
		resolveStartButtonVisibility();
		highlightLabelsForWarning(Boolean.TRUE);
		incompleteMigrationLabel.setVisible(false);
		setNotSelectedSourceAndTargetLabels();
	}

	private void setNotSelectedSourceAndTargetLabels()
	{
		final String emptyLabel = getLabel(EMPTY_CONFIG_VALUE_LABEL);
		targetStorageLabel.setValue(formatLabels(MIGRATION_WIDGET_TARGET_PATH, emptyLabel));
		sourceStorageLabel.setValue(formatLabels(MIGRATION_WIDGET_SOURCE_PATH, emptyLabel));
	}

	private void highlightLabelsForWarning(final boolean isHighlightingNeeded)
	{
		final String styleClass = !isHighlightingNeeded ? WARNING_LABEL_MESSAGE_CLASS : PRIMARY_WIDGET_LABEL;

		sourceStorageLabel.setClass(styleClass);
		targetStorageLabel.setClass(styleClass);
		matchStoragesWarningLabel.setVisible(!isHighlightingNeeded);
	}

	private ListModelList<MediaFolderData> getAllFolders()
	{
		final List<MediaFolderData> mediaFolders = mediaFolderFacade.getAllMediaFolders();

		return mediaFolders.stream()
				.filter(MediaFolderData::isCanMigrate)
				.collect(Collectors.toCollection(ListModelList::new));
	}

	private void resetFoldersCombobox()
	{
		final ListModelList<MediaFolderData> allFolders = getAllFolders();
		mediaFoldersComboBox.setModel(allFolders);
	}

	private void resolveSourceStorageLabelValue(MediaFolderData selectedFolderData)
	{
		if (selectedFolderData != null)
		{
			if (!selectedFolderData.getStorageConfigCode().equals("localStorageConfig"))
			{
				sourceStorageLabel.setValue(formatLabels(MIGRATION_WIDGET_SOURCE_PATH,
						formatLabels(MIGRATION_WIDGET_ACCOUNT, selectedFolderData.getStorageTypeName(),
								selectedFolderData.getStorageConfigName())));
			}
			else
			{
				sourceStorageLabel.setValue(formatLabels(MIGRATION_WIDGET_SOURCE_PATH, getLabel(MIGRATION_WIDGET_LOCAL_PATH)));
			}
		}
		else
		{
			sourceStorageLabel.setValue(formatLabels(MIGRATION_WIDGET_SOURCE_PATH, getLabel(EMPTY_CONFIG_VALUE_LABEL)));
		}
	}
}
