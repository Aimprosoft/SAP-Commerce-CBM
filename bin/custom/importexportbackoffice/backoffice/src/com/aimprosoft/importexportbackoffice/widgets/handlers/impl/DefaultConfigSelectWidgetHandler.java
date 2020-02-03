package com.aimprosoft.importexportbackoffice.widgets.handlers.impl;

import static com.aimprosoft.importexportbackoffice.constants.ImportexportbackofficeConstants.SELECTED_CONFIG_ATTRIBUTE_CODE;
import static com.aimprosoft.importexportbackoffice.widgets.storageconfigwidget.DefaultWidgetHelper.CONNECTION_STATUS;
import static com.aimprosoft.importexportbackoffice.widgets.storageconfigwidget.DefaultWidgetHelper.OUTPUT_SOCKET_OUTPUT_STORAGE_CONFIG_DATA;

import de.hybris.platform.servicelayer.session.SessionService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportbackoffice.widgets.handlers.WidgetHandler;
import com.aimprosoft.importexportbackoffice.widgets.state.WidgetUIContext;
import com.aimprosoft.importexportbackoffice.widgets.storageconfigwidget.WidgetHelper;
import com.aimprosoft.importexportcloud.exceptions.IemException;
import com.aimprosoft.importexportcloud.facades.CloudStorageFacade;
import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


public class DefaultConfigSelectWidgetHandler implements WidgetHandler
{
	private static final Logger LOGGER = Logger.getLogger(DefaultConfigSelectWidgetHandler.class);

	private transient WidgetHandler preAuthWidgetHandler;

	private StorageConfigFacade storageConfigFacade;

	private CloudStorageFacade cloudStorageFacade;

	private NotificationService notificationService;

	private SessionService sessionService;

	private WidgetHelper widgetHelper;

	@Override
	public void handle(final WidgetUIContext widgetUIContext)
	{
		final WidgetInstanceManager widgetInstanceManager = widgetUIContext.getWidgetInstanceManager();
		final StorageConfigData selectedConfigData = widgetUIContext.getStorageConfigData();
		final boolean isResetNeeded = widgetUIContext.isResetNeeded();

		//it is used to map auth code with related config during OAUTH2 flow
		sessionService.setAttribute(SELECTED_CONFIG_ATTRIBUTE_CODE, selectedConfigData.getCode());

		final StorageConfigData storageConfigDataFromDB = storageConfigFacade.getStorageConfigData(selectedConfigData.getCode());
		widgetUIContext.setStorageConfigData(storageConfigDataFromDB);
		widgetHelper.setSelectedConfig(storageConfigDataFromDB, widgetInstanceManager);

		try
		{
			resolveButtonVisibility(widgetUIContext);
		}
		catch (final IemException e)
		{
			LOGGER.error(e.getMessage(), e);

			notificationService
					.notifyUser(widgetInstanceManager, CONNECTION_STATUS, NotificationEvent.Level.FAILURE, e.getMessage());

			preAuthWidgetHandler.handle(widgetUIContext);
		}

		if (isResetNeeded)
		{
			widgetHelper.sendResetAndCloseEditorArea(widgetInstanceManager);
		}

		logSelectedConfig(storageConfigDataFromDB);
		widgetInstanceManager.sendOutput(OUTPUT_SOCKET_OUTPUT_STORAGE_CONFIG_DATA, storageConfigDataFromDB);
	}

	private void resolveButtonVisibility(final WidgetUIContext widgetUIContext) throws IemException
	{
		final StorageConfigData storageConfigDataFromDB = widgetUIContext.getStorageConfigData();

		// OAUTH2 authorization flow
		if (storageConfigDataFromDB.getStorageTypeData().getIsAuthNeeded())
		{
			if (storageConfigDataFromDB.getIsConnected())
			{
				cloudStorageFacade.checkAccessToken(storageConfigDataFromDB);

				widgetUIContext.renderConnectedButtons();
			}
			else
			{
				preAuthWidgetHandler.handle(widgetUIContext);
			}
		}
		else
		{
			if (Boolean.TRUE.equals(storageConfigDataFromDB.getIsConnected()))
			{
				widgetUIContext.renderConnectedButtons();
			}
			else
			{
				widgetUIContext.renderConnectAndActionsButtons();
			}
		}
	}

	public WidgetHandler getPreAuthWidgetHandler()
	{
		return preAuthWidgetHandler;
	}

	@Required
	public void setPreAuthWidgetHandler(final WidgetHandler preAuthWidgetHandler)
	{
		this.preAuthWidgetHandler = preAuthWidgetHandler;
	}

	private void logSelectedConfig(final StorageConfigData storageConfig)
	{
		if (LOGGER.isDebugEnabled())
		{
			final String name = storageConfig.getName();
			final String code = storageConfig.getStorageTypeData().getCode();
			final String appKey = storageConfig.getAppKey();
			final Boolean isAuthNeeded = storageConfig.getStorageTypeData().getIsAuthNeeded();

			LOGGER.debug(String.format("Selected storage config: %s, type: %s, appKey: %s, with auth: %s", name, code, appKey,
					isAuthNeeded));
		}
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

	public CloudStorageFacade getCloudStorageFacade()
	{
		return cloudStorageFacade;
	}

	@Required
	public void setCloudStorageFacade(final CloudStorageFacade cloudStorageFacade)
	{
		this.cloudStorageFacade = cloudStorageFacade;
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

	public SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
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
