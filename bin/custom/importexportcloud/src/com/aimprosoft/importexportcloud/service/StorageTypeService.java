package com.aimprosoft.importexportcloud.service;

import com.aimprosoft.importexportcloud.model.StorageTypeModel;

import java.util.List;

public interface StorageTypeService
{
    /**
     * Gets a list of storage type models.
     * If none are found an empty list is returned.
     *
     * @return a list of storage type models
     */
    List<StorageTypeModel> getAllStorageTypes();

    /**
     * Gets the storage type for the given code.
     *
     * @param code the code to search for storage type
     * @return storage type for code
     * @throws de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException   in case no storage type for the given code can be found
     * @throws de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException in case more than one storage type is found for the given code
     */
    StorageTypeModel getStorageTypeByCode(String code);
}
