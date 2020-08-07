package com.aimprosoft.importexportcloud.export.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateIfSingleResult;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static java.lang.String.format;

import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.export.ExportConfigurationService;
import com.aimprosoft.importexportcloud.jalo.IemExportConfiguration;
import com.aimprosoft.importexportcloud.model.IemExportConfigurationModel;


public class DefaultExportConfigurationService implements ExportConfigurationService
{
	private static final String DEFAULT_CONFIGURATION_CODE = "defaultIemExportConfiguration";
	private static final Logger LOGGER = Logger.getLogger(DefaultExportConfigurationService.class);

	private GenericDao<IemExportConfigurationModel> exportConfigurationDao;

	@Override
	public IemExportConfigurationModel getIemExportConfigurationByCode(final String code)
	{
		validateParameterNotNullStandardMessage(IemExportConfiguration.CODE, code);

		final Map<String, Object> searchParameterMap = Collections.singletonMap(IemExportConfiguration.CODE, code);

		final List<IemExportConfigurationModel> iemExportConfigurationList = exportConfigurationDao.find(searchParameterMap);

		validateIfSingleResult(iemExportConfigurationList, format("IemExportConfiguration with code '%s' not found!", code),
				format("IemExportConfiguration code '%s' is not unique, %d configs found!", code, iemExportConfigurationList.size()));

		return iemExportConfigurationList.get(0);
	}

	@Override
	public IemExportConfigurationModel getDefaultIemExportConfiguration()
	{
		return getIemExportConfigurationByCode(DEFAULT_CONFIGURATION_CODE);
	}

	@Override
	public Collection<String> getClassificationAdditionalTypes()
	{
		return getTypeCodesInternal(() -> getDefaultIemExportConfiguration().getClassificationTypeCodes(),
				"classification catalog");
	}

	@Override
	public Collection<String> getProductAdditionalTypes()
	{
		return getTypeCodesInternal(() -> getDefaultIemExportConfiguration().getProductTypeCodes(), "product catalog");
	}

	@Override
	public Collection<String> getContentAdditionalTypes()
	{
		return getTypeCodesInternal(() -> getDefaultIemExportConfiguration().getContentTypeCodes(), "content catalog");

	}

	@Override
	public Collection<String> getSiteAdditionalTypes()
	{
		return getTypeCodesInternal(() -> getDefaultIemExportConfiguration().getSiteTypeCodes(), "site");
	}

	private Collection<String> getTypeCodesInternal(final Supplier<Collection<String>> typeCodesSupplier, final String target)
	{
		Collection<String> result = Collections.emptyList();

		try
		{
			result = typeCodesSupplier.get();
		}
		catch (final UnknownIdentifierException e)
		{
			LOGGER.warn(format("No default export configuration found. Additional export types for %s won't be set.", target));
		}

		return result;
	}

	public GenericDao<IemExportConfigurationModel> getExportConfigurationDao()
	{
		return exportConfigurationDao;
	}

	@Required
	public void setExportConfigurationDao(final GenericDao<IemExportConfigurationModel> exportConfigurationDao)
	{
		this.exportConfigurationDao = exportConfigurationDao;
	}
}
