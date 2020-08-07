package com.aimprosoft.importexportcloud.export.filters.impl;

import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import de.hybris.platform.ordersplitting.model.VendorModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.store.BaseStoreModel;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class WarehouseRelatedEntriesExportFilter extends AbstractSiteRelatedExportFilter
{
	private static final Logger LOG = Logger.getLogger(WarehouseRelatedEntriesExportFilter.class);
	private Set<String> filesToFilterByVendor;
	private Set<String> filesToFilterByWarehouse;

	@Override
	public void filter(final ExportTaskInfoModel exportTaskInfoModel, final Path exportImpexAndCSVsFilePath) throws ExportException
	{
		final Collection<BaseStoreModel> stores = exportTaskInfoModel.getSite().getStores();
		final List<WarehouseModel> warehousesModels = stores.stream()
				.map(BaseStoreModel::getWarehouses)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());

		final List<String> warehouseCodes = warehousesModels.stream()
				.map(WarehouseModel::getCode)
				.collect(Collectors.toList());

		final List<String> vendorCodes = warehousesModels.stream()
				.map(WarehouseModel::getVendor)
				.map(VendorModel::getCode)
				.collect(Collectors.toList());

		try (final FileSystem zipFileSystem = FileSystems.newFileSystem(exportImpexAndCSVsFilePath, null))
		{
			processFilter(filesToFilterByVendor, zipFileSystem, vendorCodes);
			processFilter(filesToFilterByWarehouse, zipFileSystem, warehouseCodes);
		}
		catch (IOException e)
		{
			throw new ExportException("An error occurred while cleaning file", e);
		}
	}

	private void processFilter(final Set<String> filesToFilter, final FileSystem zipFileSystem,
			final List<String> filterCriteria) throws IOException
	{
		for (String fileName : filesToFilter)
		{
			final Path targetFilePath = zipFileSystem.getPath(fileName);

			Predicate<String> catalogPredicate = line -> filterCriteria.stream().anyMatch(line::contains);
			final List<String> filteredLines = filterFileLines(targetFilePath, catalogPredicate);

			logDebug(LOG, "Filter processed for file: %s ", targetFilePath.getFileName());

			copyIntoTargetZip(filteredLines, targetFilePath);
		}
	}

	@Required
	public void setFilesToFilterByVendor(final Set<String> filesToFilterByVendor)
	{
		this.filesToFilterByVendor = filesToFilterByVendor;
	}

	@Required
	public void setFilesToFilterByWarehouse(final Set<String> filesToFilterByWarehouse)
	{
		this.filesToFilterByWarehouse = filesToFilterByWarehouse;
	}
}
