package com.aimprosoft.importexportcloud.service;

import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;

import java.util.Collection;


public interface IemCMSSiteService extends CMSSiteService
{
	/**
	 * Gets cms site model.
	 *
	 * @param cmsSiteUid UID for cms site model
	 * @return cms site or null if not found
	 */
	CMSSiteModel getCMSSiteForUid(String cmsSiteUid);

	/**
	 * Gets all catalog models for specified CMS site.
	 *
	 * @param cmsSiteModel CMS site model
	 * @return collection of catalog models
	 */
	Collection<CatalogModel> getAllCatalogsForCmsSite(CMSSiteModel cmsSiteModel);
}
