package com.aimprosoft.importexportcloud.service;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;

import java.util.Collection;
import java.util.function.Supplier;


public interface IemCatalogVersionService
{
	/**
	 * Gets catalog version model disabling search restrictions.
	 *
	 * @param catalogId          catalog identifier
	 * @param catalogVersionName catalog version name
	 * @return catalog version model
	 */
	CatalogVersionModel getCatalogVersionModelWithoutSearchRestrictions(String catalogId, String catalogVersionName);

	/**
	 * Gets catalog version model disabling search restrictions.
	 *
	 * @param catalogIdAndVersionName catalog identifier and catalog version name separated by colon
	 * @return catalog version model
	 */
	CatalogVersionModel getCatalogVersionModelWithoutSearchRestrictions(String catalogIdAndVersionName);

	/**
	 * Gets all catalog versions for cms site and filters them to get all classification catalog versions
	 * and staged catalog version for all other catalogs.
	 *
	 * @param cmsSiteModel cms site mode to get catalog version for
	 * @return collection of catalog versions applicable for exporting
	 */
	Collection<CatalogVersionModel> getExportableCatalogVersionsForCmsSite(CMSSiteModel cmsSiteModel);

	/**
	 * Gets all classification catalog versions and staged catalog version for all other catalogs
	 *
	 * @return collection of catalog versions applicable for exporting
	 */
	Collection<CatalogVersionModel> getAllExportableCatalogVersions();

	/**
	 * Gets supplier results temporary using particular catalog version models
	 *
	 * @param supplier             action to be executed
	 * @param catalogVersionModels catalog version models to be set temporary in current session
	 * @param <T>                  type of the result
	 * @return supplier results
	 */
	<T> T executeWithCatalogVersions(Supplier<T> supplier, Collection<CatalogVersionModel> catalogVersionModels);
}
