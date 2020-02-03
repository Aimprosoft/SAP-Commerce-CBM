package com.aimprosoft.importexportbackoffice.widgets.handlers.impl;

import static com.aimprosoft.importexportbackoffice.constants.ImportexportbackofficeConstants.DROPBOX_AUTH_CODE_ATTRIBUTE;

import de.hybris.platform.servicelayer.session.SessionService;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportbackoffice.widgets.handlers.WidgetHandler;
import com.aimprosoft.importexportbackoffice.widgets.state.WidgetUIContext;
import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.CloudStorageFacade;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.providers.DropboxConnectionProvider;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


public class DefaultPreAuthWidgetHandler implements WidgetHandler
{
	private static final Logger LOGGER = Logger.getLogger(DefaultPreAuthWidgetHandler.class);

	private CloudStorageFacade cloudStorageFacade;

	private NotificationService notificationService;

	private SessionService sessionService;

	private DropboxConnectionProvider dropboxConnectionProvider;

	@Override
	public void handle(final WidgetUIContext widgetUIContext)
	{
		final StorageConfigData storageConfigData = widgetUIContext.getStorageConfigData();
		final HttpSession httpSession = widgetUIContext.getHttpSession();
		final WidgetInstanceManager widgetInstanceManager = widgetUIContext.getWidgetInstanceManager();

		final String[] authCodeArray = getSessionService().getAttribute(DROPBOX_AUTH_CODE_ATTRIBUTE);
		final String authCode = dropboxConnectionProvider.resolveAuthCode(authCodeArray, storageConfigData.getCode());

		if (StringUtils.isBlank(authCode))
		{
			String authURL = null;
			try
			{
				authURL = getCloudStorageFacade().getAuthURL(storageConfigData, httpSession);
			}
			catch (final CloudStorageException e)
			{
				LOGGER.error(e.getMessage(), e);
				getNotificationService().notifyUser(widgetInstanceManager, "storageConfig", NotificationEvent.Level.FAILURE,
						widgetInstanceManager.getLabel("notification.area.storage.config.failure"));
			}
			finally
			{
				widgetUIContext.renderDropBoxConnectButton(authURL);
			}
		}
		else
		{
			widgetUIContext.renderConnectAndActionsButtons();
		}
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

	@Required
	public void setDropboxConnectionProvider(
			final DropboxConnectionProvider dropboxConnectionProvider)
	{
		this.dropboxConnectionProvider = dropboxConnectionProvider;
	}
}
