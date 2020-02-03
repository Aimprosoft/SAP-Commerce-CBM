package com.aimprosoft.importexportcloud.export;

import com.aimprosoft.importexportcloud.model.IemExportConfigurationModel;

import java.util.Collection;


public interface ExportConfigurationService
{
	/**
	 * Gets export configuration for code.
	 *
	 * @param code code of export configuration
	 * @return export configuration model
	 */
	IemExportConfigurationModel getIemExportConfigurationByCode(String code);

	/**
	 * Gets default export configuration.
	 *
	 * @return default export configuration model
	 */
	IemExportConfigurationModel getDefaultIemExportConfiguration();

	/**
	 * Gets collection of additional ComposedType codes for classification catalog from default export configuration.
	 *
	 * @return collection of ComposedType codes
	 */
	Collection<String> getClassificationAdditionalTypes();

	/**
	 * Gets collection of additional ComposedType codes for product catalog from default export configuration.
	 *
	 * @return collection of ComposedType codes
	 */
	Collection<String> getProductAdditionalTypes();

	/**
	 * Gets collection of additional ComposedType codes for content catalog from default export configuration.
	 *
	 * @return collection of ComposedType codes
	 */
	Collection<String> getContentAdditionalTypes();

	/**
	 * Gets collection of additional ComposedType codes for site from default export configuration.
	 *
	 * @return collection of ComposedType codes
	 */
	Collection<String> getSiteAdditionalTypes();
}
