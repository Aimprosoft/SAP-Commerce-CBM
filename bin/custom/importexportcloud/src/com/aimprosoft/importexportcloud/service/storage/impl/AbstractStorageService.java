package com.aimprosoft.importexportcloud.service.storage.impl;

import static com.aimprosoft.importexportcloud.constants.ImportexportcloudConstants.STORAGE_PATH_SEPARATOR;

import de.hybris.platform.servicelayer.internal.service.AbstractBusinessService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.service.storage.StorageService;
import com.aimprosoft.importexportcloud.service.validators.StorageConfigValidator;


public abstract class AbstractStorageService extends AbstractBusinessService implements StorageService
{
	private static final String TEMP_DIRECTORY_NAME = "importexport";
	private static final String DOWNLOAD_FILE_PREFIX = "import-";
	private static final String IMPORT_FILE_SUFFIX = ".hyb";

	private StorageConfigValidator storageConfigValidator;

	protected Path getDownloadedFilePath(final InputStream inputStream) throws CloudStorageException
	{
		final String tmpDirectoryPath = System.getProperty("HYBRIS_TEMP_DIR");
		final Path tempDirectory = Paths.get(tmpDirectoryPath + STORAGE_PATH_SEPARATOR + TEMP_DIRECTORY_NAME);

		try
		{
			Files.createDirectories(tempDirectory);
			final Path result = Files.createTempFile(tempDirectory, DOWNLOAD_FILE_PREFIX, IMPORT_FILE_SUFFIX);
			Files.copy(inputStream, result, StandardCopyOption.REPLACE_EXISTING);
			return result;
		}
		catch (final IOException e)
		{
			throw new CloudStorageException("An error occurred while operating with local file or directory.", e);
		}
	}

	protected void logUploadingFile(final Logger logger, final TaskInfoData taskInfoData)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug(String.format("Uploading file '%s' to '%s' path: %s, from: %s, account name: %s, scope: %s",
					taskInfoData.getRealFileName(), taskInfoData.getConfig().getStorageTypeData().getCode(),
					taskInfoData.getCloudUploadFolderPath(), taskInfoData.getFileToUploadPath(),
					taskInfoData.getConfig().getName(), taskInfoData.getTaskInfoScopeCode()));
		}
	}

	public StorageConfigValidator getStorageConfigValidator()
	{
		return storageConfigValidator;
	}

	@Required
	public void setStorageConfigValidator(final StorageConfigValidator storageConfigValidator)
	{
		this.storageConfigValidator = storageConfigValidator;
	}
}
