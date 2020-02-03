package com.aimprosoft;

import static com.aimprosoft.importexportbackoffice.constants.ImportexportbackofficeConstants.DROPBOX_AUTH_CODE_ATTRIBUTE;
import static com.aimprosoft.importexportbackoffice.constants.ImportexportbackofficeConstants.SELECTED_CONFIG_ATTRIBUTE_CODE;

import de.hybris.platform.jalo.JaloSession;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


public class DropboxResponseHandlerServlet extends HttpServlet
{
	private static final Logger LOGGER = Logger.getLogger(DropboxResponseHandlerServlet.class);

	@Override
	protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
	{
		final String authCode = req.getParameter("code");
		final String state = req.getParameter("state");
		final String dropboxCsrfToken = (String) req.getSession().getAttribute("dropbox-auth-csrf-token");

		if (StringUtils.isNotBlank(authCode) && StringUtils.isNotBlank(state) && state.equals(dropboxCsrfToken))
		{
			final String configCode = (String) JaloSession.getCurrentSession().getAttribute(SELECTED_CONFIG_ATTRIBUTE_CODE);
			final String[] authCodeArray = new String[] { configCode, authCode };

			JaloSession.getCurrentSession().setAttribute(DROPBOX_AUTH_CODE_ATTRIBUTE, authCodeArray);

			LOGGER.info("DropBox authorization code has been obtained. Code is " + authCode);
		}
		else
		{
			final String errorDescription = req.getParameter("error_description");
			LOGGER.error(
					"Couldn't obtain Dropbox authorization code from the request. Error description:" + (errorDescription != null ?
							errorDescription :
							""));
		}
		resp.sendRedirect("/backoffice/");
	}
}
