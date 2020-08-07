package com.aimprosoft.importexportbackoffice.widgets.sourceselectorwidget;

import com.aimprosoft.importexportcloud.enums.TaskInfoScope;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.service.IemCatalogVersionService;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.util.DefaultWidgetController;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.site.BaseSiteService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.ListModelList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@SuppressWarnings({ "unused" })
public class SourceSelectorWidgetController extends DefaultWidgetController //NOSONAR
{
	private static final String SELECTED_CATALOG_INDEX = "selectedCatalogIndex";
	private static final String SELECTED_SITE_INDEX = "selectedSiteIndex";
	private static final String OUTPUT_SITE = "outputSite";
	private static final String OUTPUT_CATALOG = "outputCatalog";
	private static final String CATALOG_OPTIONS_DELIMITER = ":";
	private static final String SELECTED_SCOPE = "selectedScope";

	private final Map<String, Combobox> comboboxMap = new HashMap<>();
	private Combobox siteTypesComboBox;
	private Combobox catalogTypesComboBox;

	@WireVariable
	private transient BaseSiteService baseSiteService;

	@WireVariable
	private IemCatalogVersionService iemCatalogVersionService;

	@Override
	public void initialize(final Component component)
	{
		final Collection<CatalogVersionModel> allCatalogs = iemCatalogVersionService.getAllExportableCatalogVersions();
		catalogTypesComboBox.setModel(new ListModelList<>(allCatalogs));

		final Collection<BaseSiteModel> allSites = baseSiteService.getAllBaseSites();
		siteTypesComboBox.setModel(new ListModelList<>(allSites));

		comboboxMap.put(TaskInfoScope.CATALOGSCOPE.getCode(), catalogTypesComboBox);
		comboboxMap.put(TaskInfoScope.SITESCOPE.getCode(), siteTypesComboBox);

		final String selectedScope = getSelectedScope();
		final String scope = selectedScope != null ? selectedScope : TaskInfoScope.SITESCOPE.getCode();
		resolveDropDownRender(scope);
	}

	@SocketEvent(socketId = "reset")
	public void onReset(final StorageConfigData storageConfigData)
	{
		resetComboBoxes(null);
	}

	@SocketEvent(socketId = "selectedScope")
	public void onSelectScope(final String inputSelectedScope)
	{
		resolveDropDownRender(inputSelectedScope);
		setSelectedScope(inputSelectedScope);
		resetComboBoxes(inputSelectedScope);
	}

	@ViewEvent(eventName = Events.ON_SELECT, componentID = "siteTypesComboBox")
	public void onSelectSite()
	{
		final String siteUid = getSiteUid();
		setSelectedSiteIndex(siteTypesComboBox.getSelectedIndex());
		sendOutput(OUTPUT_SITE, siteUid);
	}

	@ViewEvent(eventName = Events.ON_SELECT, componentID = "catalogTypesComboBox")
	public void onSelectCatalog()
	{
		final String catalogCodeAndVersion = obtainCatalogCodeVersion();
		setSelectedCatalogIndex(catalogTypesComboBox.getSelectedIndex());
		sendOutput(OUTPUT_CATALOG, catalogCodeAndVersion);
	}

	@ViewEvent(eventName = "onAfterRender", componentID = "siteTypesComboBox")
	public void onSiteComboBoxAfterRender()
	{
		final Integer selectedSiteIndex = getSelectedSiteIndex();
		if (selectedSiteIndex != null)
		{
			siteTypesComboBox.setSelectedIndex(selectedSiteIndex);
			final String siteUid = getSiteUid();
			sendOutput(OUTPUT_SITE, siteUid);
		}
	}

	@ViewEvent(eventName = "onAfterRender", componentID = "catalogTypesComboBox")
	public void onCatalogComboBoxAfterRender()
	{
		final Integer selectedCatalogIndex = getSelectedCatalogIndex();
		if (selectedCatalogIndex != null)
		{
			catalogTypesComboBox.setSelectedIndex(selectedCatalogIndex);
			final String catalogCodeAndVersion = obtainCatalogCodeVersion();
			sendOutput(OUTPUT_CATALOG, catalogCodeAndVersion);
		}
	}

	private String getSelectedScope()
	{
		return this.getWidgetInstanceManager().getModel().getValue(SELECTED_SCOPE, String.class);
	}

	private void setSelectedScope(final String selectedScope)
	{
		this.getWidgetInstanceManager().getModel().put(SELECTED_SCOPE, selectedScope);
	}

	private Integer getSelectedCatalogIndex()
	{
		return this.getWidgetInstanceManager().getModel().getValue(SELECTED_CATALOG_INDEX, Integer.class);
	}

	private Integer getSelectedSiteIndex()
	{
		return this.getWidgetInstanceManager().getModel().getValue(SELECTED_SITE_INDEX, Integer.class);
	}

	private void setSelectedCatalogIndex(final Integer selectedCatalogIndex)
	{
		this.getWidgetInstanceManager().getModel().put(SELECTED_CATALOG_INDEX, selectedCatalogIndex);
	}

	private void setSelectedSiteIndex(final Integer selectedSiteIndex)
	{
		this.getWidgetInstanceManager().getModel().put(SELECTED_SITE_INDEX, selectedSiteIndex);
	}

	private String getSiteUid()
	{
		final CMSSiteModel site = siteTypesComboBox.getSelectedItem().getValue();
		return site.getUid();
	}

	private String obtainCatalogCodeVersion()
	{
		final CatalogVersionModel catalogVersion = catalogTypesComboBox.getSelectedItem().getValue();
		final CatalogModel catalog = catalogVersion.getCatalog();
		return catalog.getId() + CATALOG_OPTIONS_DELIMITER + catalogVersion.getVersion();
	}

	private void resolveDropDownRender(final String scope)
	{
		comboboxMap.forEach((s, combobox) -> combobox.getParent().setVisible(Boolean.FALSE));
		comboboxMap.get(scope).getParent().setVisible(Boolean.TRUE);
	}

	private void resetComboBoxes(final String scope)
	{
		catalogTypesComboBox.setSelectedItem(null);
		siteTypesComboBox.setSelectedItem(null);
		setSelectedCatalogIndex(null);
		setSelectedCatalogIndex(null);
		resolveDropDownRender(scope != null ? scope : TaskInfoScope.SITESCOPE.getCode());
	}
}
