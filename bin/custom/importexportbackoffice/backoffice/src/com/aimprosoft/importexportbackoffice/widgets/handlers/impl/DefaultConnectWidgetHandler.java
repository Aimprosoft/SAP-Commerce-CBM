package com.aimprosoft.importexportbackoffice.widgets.handlers.impl;

import static com.aimprosoft.importexportbackoffice.widgets.storageconfigwidget.DefaultWidgetHelper.CONNECTION_STATUS;
import static com.aimprosoft.importexportbackoffice.widgets.storageconfigwidget.DefaultWidgetHelper.OUTPUT_SOCKET_OUTPUT_STORAGE_CONFIG_DATA;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportbackoffice.widgets.handlers.WidgetHandler;
import com.aimprosoft.importexportbackoffice.widgets.state.WidgetUIContext;
import com.aimprosoft.importexportbackoffice.widgets.storageconfigwidget.WidgetHelper;
import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.CloudStorageFacade;
import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


public class DefaultConnectWidgetHandler implements WidgetHandler
{
	private static final Logger LOGGER = Logger.getLogger(DefaultConnectWidgetHandler.class);

	private NotificationService notificationService;

	private CloudStorageFacade cloudStorageFacade;

	private StorageConfigFacade storageConfigFacade;

	private WidgetHelper widgetHelper;

	@Override
	public void handle(final WidgetUIContext widgetUIContext)
	{
		StorageConfigData storageConfigData = widgetUIContext.getStorageConfigData();
		final WidgetInstanceManager widgetInstanceManager = widgetUIContext.getWidgetInstanceManager();

		try
		{
			final StorageConfigData storageConfigDataFromDB = storageConfigFacade.getStorageConfigData(storageConfigData.getCode());

			if (Boolean.FALSE.equals(storageConfigDataFromDB.getIsConnected()))
			{
				cloudStorageFacade.connect(storageConfigData);

				storageConfigData = storageConfigFacade.getStorageConfigData(storageConfigData.getCode());
				widgetUIContext.setStorageConfigData(storageConfigData);
				widgetHelper.setSelectedConfig(storageConfigData, widgetInstanceManager);
			}

			notificationService.notifyUser(widgetInstanceManager, CONNECTION_STATUS, NotificationEvent.Level.SUCCESS);

			widgetUIContext.renderConnectedButtons();
		}
		catch (final CloudStorageException e)
		{
			LOGGER.error(e.getMessage(), e);
			notificationService
					.notifyUser(widgetInstanceManager, CONNECTION_STATUS, NotificationEvent.Level.FAILURE, e.getMessage());
		}

		widgetInstanceManager.sendOutput(OUTPUT_SOCKET_OUTPUT_STORAGE_CONFIG_DATA, storageConfigData);
	}

	public NotificationService getNotificationService()
	{
		return notificationService;
	}

	@Required
	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}

	public CloudStorageFacade getCloudStorageFacade()
	{
		return cloudStorageFacade;
	}

	@Required
	public void setCloudStorageFacade(final CloudStorageFacade cloudStorageFacade)
	{
		this.cloudStorageFacade = cloudStorageFacade;
	}

	public StorageConfigFacade getStorageConfigFacade()
	{
		return storageConfigFacade;
	}

	@Required
	public void setStorageConfigFacade(final StorageConfigFacade storageConfigFacade)
	{
		this.storageConfigFacade = storageConfigFacade;
	}

	public WidgetHelper getWidgetHelper()
	{
		return widgetHelper;
	}

	@Required
	public void setWidgetHelper(final WidgetHelper widgetHelper)
	{
		this.widgetHelper = widgetHelper;
	}

}
