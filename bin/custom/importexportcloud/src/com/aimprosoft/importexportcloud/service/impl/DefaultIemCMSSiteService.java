package com.aimprosoft.importexportcloud.service.impl;

import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.impl.DefaultCMSSiteService;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.service.IemCMSSiteService;
import com.aimprosoft.importexportcloud.service.IemSessionService;


public class DefaultIemCMSSiteService extends DefaultCMSSiteService implements IemCMSSiteService
{
	private IemSessionService iemSessionService;

	@Override
	public CMSSiteModel getCMSSiteForUid(final String cmsSiteUid)
	{
		if (cmsSiteUid != null)
		{
			return (CMSSiteModel) this.getBaseSiteService().getBaseSiteForUID(cmsSiteUid);
		}

		return null;
	}

	@Override
	public Collection<CatalogModel> getAllCatalogsForCmsSite(final CMSSiteModel cmsSiteModel)
	{
		return iemSessionService.executeWithoutSearchRestrictions(() -> getAllCatalogs(cmsSiteModel));
	}

	public IemSessionService getIemSessionService()
	{
		return iemSessionService;
	}

	@Required
	public void setIemSessionService(final IemSessionService iemSessionService)
	{
		this.iemSessionService = iemSessionService;
	}
}
