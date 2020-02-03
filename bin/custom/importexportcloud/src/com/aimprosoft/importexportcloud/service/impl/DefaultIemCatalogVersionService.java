package com.aimprosoft.importexportcloud.service.impl;

import de.hybris.platform.catalog.impl.DefaultCatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.catalog.model.classification.ClassificationSystemVersionModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;

import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.service.IemCMSSiteService;
import com.aimprosoft.importexportcloud.service.IemCatalogVersionService;
import com.aimprosoft.importexportcloud.service.IemSessionService;


public class DefaultIemCatalogVersionService extends DefaultCatalogVersionService implements IemCatalogVersionService
{
	private String stagedCatalogVersionName;

	private IemSessionService iemSessionService;

	private IemCMSSiteService iemCMSSiteService;

	@Override
	public CatalogVersionModel getCatalogVersionModelWithoutSearchRestrictions(final String catalogId, final String catalogVersionName)
	{
		return iemSessionService.executeWithoutSearchRestrictions(() -> getCatalogVersion(catalogId, catalogVersionName));
	}

	@Override
	public CatalogVersionModel getCatalogVersionModelWithoutSearchRestrictions(final String catalogIdAndVersionName)
	{
		if (catalogIdAndVersionName == null)
		{
			return null;
		}

		final String[] catalogAndVersion = obtainCatalogIdAndVersionName(catalogIdAndVersionName);

		return getCatalogVersionModelWithoutSearchRestrictions(catalogAndVersion[0], catalogAndVersion[1]);
	}

	@Override
	public Collection<CatalogVersionModel> getExportableCatalogVersionsForCmsSite(final CMSSiteModel cmsSiteModel)
	{
		final Set<CatalogVersionModel> catalogVersionModels = iemSessionService
				.executeWithoutSearchRestrictions(() -> iemCMSSiteService.getAllCatalogsForCmsSite(cmsSiteModel).stream()
						.flatMap(catalogModel -> catalogModel.getCatalogVersions().stream()).collect(Collectors.toSet()));

		return filterClassificationAndStagedCatalogVersions(catalogVersionModels);
	}

	@Override
	public Collection<CatalogVersionModel> getAllExportableCatalogVersions()
	{
		final Collection<CatalogVersionModel> catalogVersionModels = iemSessionService
				.executeWithoutSearchRestrictions(this::getAllCatalogVersions);

		return filterClassificationAndStagedCatalogVersions(catalogVersionModels);
	}

	@Override
	public <T> T executeWithCatalogVersions(final Supplier<T> supplier, final Collection<CatalogVersionModel> catalogVersionModels)
	{
		final Collection<CatalogVersionModel> currentCatalogVersions = getSessionCatalogVersions();

		try
		{
			setSessionCatalogVersions(catalogVersionModels);

			return supplier.get();
		}
		finally
		{
			setSessionCatalogVersions(currentCatalogVersions);
		}
	}

	private String[] obtainCatalogIdAndVersionName(final String catalogIdAndVersionName)
	{
		final String[] catalogAndVersion = catalogIdAndVersionName.split(":");

		if (catalogAndVersion.length != 2)
		{
			throw new IllegalArgumentException("Catalog version doesn't match pattern 'catalogId:versionName'.");
		}

		return catalogAndVersion;
	}

	private Collection<CatalogVersionModel> filterClassificationAndStagedCatalogVersions(
			final Collection<CatalogVersionModel> catalogVersionModels)
	{
		return catalogVersionModels.stream()
				.filter(catalogVersionModel -> (catalogVersionModel instanceof ClassificationSystemVersionModel)
						|| stagedCatalogVersionName.equals(catalogVersionModel.getVersion())).collect(Collectors.toSet());
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

	public IemCMSSiteService getIemCMSSiteService()
	{
		return iemCMSSiteService;
	}

	@Required
	public void setIemCMSSiteService(final IemCMSSiteService iemCMSSiteService)
	{
		this.iemCMSSiteService = iemCMSSiteService;
	}

	public String getStagedCatalogVersionName()
	{
		return stagedCatalogVersionName;
	}

	@Required
	public void setStagedCatalogVersionName(final String stagedCatalogVersionName)
	{
		this.stagedCatalogVersionName = stagedCatalogVersionName;
	}
}
