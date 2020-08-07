package com.aimprosoft.importexportcloud.service.impl;

import com.aimprosoft.importexportcloud.service.IemCMSSiteService;
import com.aimprosoft.importexportcloud.service.IemCatalogVersionService;
import com.aimprosoft.importexportcloud.service.IemSessionService;
import de.hybris.platform.catalog.constants.CatalogConstants;
import de.hybris.platform.catalog.impl.DefaultCatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.catalog.model.classification.ClassificationSystemVersionModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.user.UserService;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class DefaultIemCatalogVersionService extends DefaultCatalogVersionService implements IemCatalogVersionService
{
	private static final String SESSION_USER_ATTRIBUTE = "user";

	private String CBMuserUid;

	private String stagedCatalogVersionName;

	private IemSessionService iemSessionService;

	private IemCMSSiteService iemCMSSiteService;

	private UserService userService;


	@Override
	public CatalogVersionModel getCatalogVersionModelWithoutSearchRestrictions(final String catalogId,
			final String catalogVersionName)
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
		final Map<String, Object> localViewParametersMap = new HashMap<>();

		localViewParametersMap.put(CatalogConstants.SESSION_CATALOG_VERSIONS, catalogVersionModels);

		final UserModel currentUser = userService.getCurrentUser();
/*
		This approach is used to restrict access because if there are multiple sites, the administrator has access to all content
		for all sites.
		Export data can include data from other sites due to Admin doesn't have restrictions for Catalog Versions
		Because of that "CBMUser" is used to restrict Catalog Versions that are used in appropriate scope (Site, catalog)
 */
		if (userService.isAdmin(currentUser))
		{
			localViewParametersMap.put(SESSION_USER_ATTRIBUTE, userService.getUserForUID(CBMuserUid));
		}

		return getSessionService().executeInLocalViewWithParams(localViewParametersMap, new SessionExecutionBody()
		{
			@Override
			public Object execute()
			{
				return supplier.get();
			}
		});
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
				.filter(catalogVersionModel -> catalogVersionModel instanceof ClassificationSystemVersionModel
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

	public String getCBMuserUid()
	{
		return CBMuserUid;
	}

	@Required
	public void setCBMuserUid(final String CBMuserUid)
	{
		this.CBMuserUid = CBMuserUid;
	}

	public UserService getUserService()
	{
		return userService;
	}

	@Override
	public void setUserService(final UserService userService)
	{
		super.setUserService(userService);
		this.userService = userService;
	}
}
