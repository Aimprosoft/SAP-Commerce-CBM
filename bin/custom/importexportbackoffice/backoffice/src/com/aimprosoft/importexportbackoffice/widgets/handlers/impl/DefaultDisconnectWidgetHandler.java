package com.aimprosoft.importexportbackoffice.widgets.handlers.impl;


import static com.aimprosoft.importexportbackoffice.widgets.storageconfigwidget.DefaultWidgetHelper.DISCONNECT;

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


public class DefaultDisconnectWidgetHandler implements WidgetHandler
{
	private static final Logger LOGGER = Logger.getLogger(DefaultDisconnectWidgetHandler.class);

	private NotificationService notificationService;

	private CloudStorageFacade cloudStorageFacade;

	private StorageConfigFacade storageConfigFacade;

	private WidgetHelper widgetHelper;

	@Override
	public void handle(final WidgetUIContext widgetUIContext)
	{
		final WidgetInstanceManager widgetInstanceManager = widgetUIContext.getWidgetInstanceManager();
		StorageConfigData storageConfigData = widgetUIContext.getStorageConfigData();

		if (storageConfigData != null)
		{
			try
			{
				storageConfigData = getStorageConfigFacade().getStorageConfigData(storageConfigData.getCode());
				cloudStorageFacade.disconnect(storageConfigData);
				widgetUIContext.resetStorageConfigLayout();

				notificationService.notifyUser(widgetInstanceManager, DISCONNECT, NotificationEvent.Level.WARNING);
				widgetHelper.sendResetAndCloseEditorArea(widgetInstanceManager);
			}
			catch (final CloudStorageException e)
			{
				LOGGER.error(e.getMessage(), e);

				notificationService
						.notifyUser(widgetInstanceManager, DISCONNECT, NotificationEvent.Level.FAILURE, e.getMessage());
			}
		}
		else
		{
			notificationService
					.notifyUser(widgetInstanceManager, DISCONNECT, NotificationEvent.Level.FAILURE,
							widgetInstanceManager.getLabel("storageconfig.notification.disconnect.fail"));
		}
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

	public WidgetHelper getWidgetHelper()
	{
		return widgetHelper;
	}

	@Required
	public void setWidgetHelper(final WidgetHelper widgetHelper)
	{
		this.widgetHelper = widgetHelper;
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

}
