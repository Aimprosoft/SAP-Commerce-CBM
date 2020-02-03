package com.aimprosoft.importexportcloud.facades;

import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;

import java.util.List;


public interface StorageTypeFacade
{
	/**
	 * Gets all storage types.
	 *
	 * @return all existing storage types
	 */
	List<StorageTypeData> getStorageTypesData();
}
