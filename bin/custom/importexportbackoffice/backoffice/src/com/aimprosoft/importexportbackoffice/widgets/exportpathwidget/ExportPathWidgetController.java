package com.aimprosoft.importexportbackoffice.widgets.exportpathwidget;

import com.aimprosoft.importexportbackoffice.widgets.importpathwidget.AbstractPathWidgetController;
import com.aimprosoft.importexportcloud.enums.TaskInfoScope;
import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.media.MediaService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Textbox;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@SuppressWarnings({ "unused" })
public class ExportPathWidgetController extends AbstractPathWidgetController
{
	private static final Logger LOG = Logger.getLogger(ExportPathWidgetController.class);
	private static final String EXPORTSTART = "exportstart";
	private static final String EXPORTBLOCKED = "exportblocked";
	private static final String OUTPUT_SOCKET_RESET = "reset";
	private static final String VALIDATION_ERROR_CLASS = "prefixvalidationerror";
	private static final String EXPORTED_MEDIA_CODE = "exportedMediaCode";
	private static final String RESULT_PREFIX_IS_VALID_ATTR = "isValid";

	private Button downloadButton;
	private Div errorMessage;
	private Textbox resultPrefixTextBox;

	@WireVariable
	private transient MediaService mediaService;

	@Override
	public void initialize(final Component comp)
	{
		super.initialize(comp);
		resultPrefixTextBox.setAttribute(RESULT_PREFIX_IS_VALID_ATTR, true);

		startButton.setLabel(getLabel("task.button.export.start"));
		pathLabel.setValue(getLabel("task.label.export.selected.path"));
		choosePathButton.setLabel(getLabel("task.button.export.choose"));
		chooseLocalPathButton.setLabel(getLabel("task.button.export.choose"));
	}

	@SocketEvent(socketId = "selectedScope")
	public void onChooseScope(final String scope)
	{
		updateTaskInfoData(null, null, scope);
		downloadButton.setVisible(false);
		clearPathLabels();
		checkChoosePathButtonAvailability();
		checkStartButtonAvailability();
	}

	@SocketEvent(socketId = "inputSite")
	public void onChooseSite(final String chosenSite)
	{
		updateTaskInfoData(chosenSite, null, TaskInfoScope.SITESCOPE.getCode());
		checkChoosePathButtonAvailability();
		checkStartButtonAvailability();
	}

	@SocketEvent(socketId = "inputCatalog")
	public void onChooseCatalog(final String chosenCatalog)
	{
		updateTaskInfoData(null, chosenCatalog, TaskInfoScope.CATALOGSCOPE.getCode());
		checkChoosePathButtonAvailability();
		checkStartButtonAvailability();
	}

	@SocketEvent(socketId = "selectedExportMediaNeeded")
	public void setSelectedExportMediaNeeded(final Boolean isExportMediaNeeded)
	{
		getOrCreateTaskInfoData().setExportMediaNeeded(isExportMediaNeeded);
	}

	@Override
	@SocketEvent(socketId = "inputStorageConfigData")
	public void onInputStorageConfigData(final StorageConfigData storageConfigData)
	{
		super.onInputStorageConfigData(storageConfigData);
		checkChoosePathButtonAvailability();

		if (!storageConfigData.getStorageTypeData().getIsLocal())
		{
			downloadButton.setVisible(false);
		}
	}

	@Override
	@SocketEvent(socketId = "inputStorageTypeData")
	public void onInputStorageTypeData(final StorageTypeData storageType)
	{
		super.onInputStorageTypeData(storageType);
		downloadButton.setVisible(false);
	}

	@ViewEvent(eventName = Events.ON_CLICK, componentID = "choosePathButton")
	public void onChooseExportPathButtonClick(final Event event)
	{
		sendOutput("outputExportConfigData", getOrCreateTaskInfoData().getConfig());
	}

	@SocketEvent(socketId = "inputChoosenObject")
	public void onChosenObject(final Map<String, String> inputMap)
	{
		fillPathLabels(inputMap);
		checkStartButtonAvailability();
	}

	@ViewEvent(eventName = Events.ON_CLICK, componentID = "startButton")
	public void onStartExportButton()
	{
		try
		{
			cloudStorageFacade.checkActiveTask();
		}
		catch (CloudStorageException e)
		{
			notifyUser(EXPORTBLOCKED, NotificationEvent.Level.FAILURE, StringUtils.EMPTY, StringUtils.EMPTY);
			return;
		}

		/* because you can catch to click start button with invalid prefix before onResultPrefixChange() method invocation */
		if (!isResultPrefixValid(resultPrefixTextBox.getValue()))
		{
			return;
		}

		final TaskInfoData taskInfoData = getOrCreateTaskInfoData();
		final StorageConfigData config = taskInfoData.getConfig();

		String filePath = realFilePathLabel.getValue();

		try
		{
			filePath = URLDecoder.decode(realFilePathLabel.getValue(), "UTF-8");
		}
		catch (final UnsupportedEncodingException e)
		{
			notifyUser(EXPORTSTART, NotificationEvent.Level.FAILURE, getLabel("export.controller.failure.folder.path.decode"));
		}

		if (filePath == null && !config.getStorageTypeData().getIsLocal())
		{
			notifyUser(EXPORTSTART, NotificationEvent.Level.FAILURE, getLabel("export.controller.failure.folder.path.blank"));
		}
		else
		{
			taskInfoData.setCloudUploadFolderPath(filePath);
			taskInfoData.setCloudUploadFolderPathToDisplay(displayPathLabel.getValue());
			taskInfoData.setResultPrefix(resultPrefixTextBox.getValue());

			exportAndUploadData(taskInfoData);

			sendOutput(OUTPUT_SOCKET_RESET, null);
		}
	}

	@ViewEvent(eventName = Events.ON_CLICK, componentID = "downloadButton")
	public void onDownload()
	{
		final MediaModel media = mediaService.getMedia((String) downloadButton.getAttribute(EXPORTED_MEDIA_CODE));
		final InputStream streamFromMedia = mediaService.getStreamFromMedia(media);

		Filedownload.save(streamFromMedia, media.getMime(), media.getRealFileName());
	}

	@ViewEvent(eventName = Events.ON_CHANGING, componentID = "resultPrefixTextBox")
	public void onResultPrefixChange(final InputEvent event)
	{
		final String value = event.getValue();
		resultPrefixTextBox.setAttribute(RESULT_PREFIX_IS_VALID_ATTR, isResultPrefixValid(value));
		checkStartButtonAvailability();
	}

	private void checkStartButtonAvailability()
	{
		startButton.setDisabled(!isExportTaskStartAllowed());
	}

	private void checkChoosePathButtonAvailability()
	{
		choosePathButton.setDisabled(!isChoosePathButtonAllowed());
	}

	private boolean isChoosePathButtonAllowed()
	{
		final TaskInfoData taskInfoData = getOrCreateTaskInfoData();
		final StorageConfigData storageConfigData = taskInfoData.getConfig();

		return storageConfigData != null && storageConfigData.getIsConnected() && isExportSourceSelected(taskInfoData);
	}

	private boolean isExportTaskStartAllowed()
	{
		final TaskInfoData taskInfoData = getOrCreateTaskInfoData();

		final StorageConfigData storageConfigData = taskInfoData.getConfig();

		/* valid prefix and selected export source are mandatory conditions
		 * and either storage is local or cloud storage path is already selected */
		final boolean isResultPrefixValid = (boolean) resultPrefixTextBox.getAttribute(RESULT_PREFIX_IS_VALID_ATTR);

		return isResultPrefixValid && isExportSourceSelected(taskInfoData)
				&& (storageConfigData != null && storageConfigData.getStorageTypeData().getIsLocal()
				|| StringUtils.isNotEmpty(realFilePathLabel.getValue()));
	}

	private void exportAndUploadData(final TaskInfoData taskInfoData)
	{
		try
		{
			cloudStorageFacade.exportData(taskInfoData);

			notifyUser(EXPORTSTART, NotificationEvent.Level.SUCCESS, getLabel("export.controller.success.data.export"));

			if (taskInfoData.getConfig().getStorageTypeData().getIsLocal())
			{
				downloadButton.setAttribute(EXPORTED_MEDIA_CODE, taskInfoData.getExportedMediaCode());
				downloadButton.setVisible(true);
			}
			else
			{
				cloudStorageFacade.upload(taskInfoData);
				cloudStorageFacade.removeTemporaryFile(taskInfoData);
			}

			notifyUser(EXPORTSTART, NotificationEvent.Level.SUCCESS, getLabel("export.controller.success.file.upload"));
		}
		catch (final ExportException e)
		{
			LOG.error(e.getMessage(), e);
			notifyUser(EXPORTSTART, NotificationEvent.Level.FAILURE, e.getMessage(), e.getTaskInfoModel());
		}
		catch (final CloudStorageException e)
		{
			LOG.error(e.getMessage(), e);
			notifyUser(EXPORTSTART, NotificationEvent.Level.FAILURE, e.getMessage(), getErrorDetails(e.getTaskInfoModel()));
		}

		setLayoutAfterExporting(taskInfoData.getConfig());
	}

	private void setLayoutAfterExporting(final StorageConfigData storageConfigData)
	{
		resultPrefixTextBox.setValue("");
		onReset();
		onInputStorageConfigData(storageConfigData);
	}

	private void updateTaskInfoData(final String cmsSiteUid, final String catalogVersion, final String taskInfoScopeCode)
	{
		final TaskInfoData taskInfoData = getOrCreateTaskInfoData();
		taskInfoData.setCmsSiteUid(cmsSiteUid);
		taskInfoData.setCatalogIdAndVersionName(catalogVersion);
		taskInfoData.setTaskInfoScopeCode(taskInfoScopeCode);
	}

	private boolean isExportSourceSelected(final TaskInfoData taskInfoData)
	{
		boolean result = false;

		final String taskInfoScopeCode = taskInfoData.getTaskInfoScopeCode();

		if (TaskInfoScope.SITESCOPE.getCode().equals(taskInfoScopeCode))
		{
			result = StringUtils.isNotBlank(taskInfoData.getCmsSiteUid());
		}
		else if (TaskInfoScope.CATALOGSCOPE.getCode().equals(taskInfoScopeCode))
		{
			result = StringUtils.isNotBlank(taskInfoData.getCatalogIdAndVersionName());
		}

		return result;
	}

	private boolean isResultPrefixValid(final String prefix)
	{
		final Matcher validMatcher = Pattern.compile("[\\w\\d_-]{0,255}").matcher(prefix);

		final boolean result = validMatcher.matches();

		toggleErrorMessage(result);

		return result;
	}

	private void toggleErrorMessage(final boolean showMessage)
	{
		if (showMessage)
		{
			errorMessage.setVisible(false);
			resultPrefixTextBox.setClass(StringUtils.EMPTY);
		}
		else
		{
			errorMessage.setVisible(true);
			resultPrefixTextBox.setClass(VALIDATION_ERROR_CLASS);
		}
	}
}
