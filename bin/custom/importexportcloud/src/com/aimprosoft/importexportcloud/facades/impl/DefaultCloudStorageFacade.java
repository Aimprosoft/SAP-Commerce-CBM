package com.aimprosoft.importexportcloud.facades.impl;

import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.exceptions.ModelRemovalException;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.enums.TaskInfoStatus;
import com.aimprosoft.importexportcloud.exceptions.CloudStorageException;
import com.aimprosoft.importexportcloud.exceptions.ExportException;
import com.aimprosoft.importexportcloud.exceptions.IemException;
import com.aimprosoft.importexportcloud.exceptions.ImportException;
import com.aimprosoft.importexportcloud.exceptions.InvalidTokenException;
import com.aimprosoft.importexportcloud.facades.CloudStorageFacade;
import com.aimprosoft.importexportcloud.facades.ExportFacade;
import com.aimprosoft.importexportcloud.facades.ImportFacade;
import com.aimprosoft.importexportcloud.facades.data.CloudObjectData;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.ImportTaskInfoModel;
import com.aimprosoft.importexportcloud.model.TaskInfoModel;
import com.aimprosoft.importexportcloud.service.TaskInfoService;
import com.aimprosoft.importexportcloud.service.connection.ConnectionService;
import com.aimprosoft.importexportcloud.service.storage.StorageService;
import com.aimprosoft.importexportcloud.service.validators.StorageConfigValidator;


public class DefaultCloudStorageFacade implements CloudStorageFacade
{
    private static final Logger LOGGER = Logger.getLogger(DefaultCloudStorageFacade.class);

    private Map<String, StorageService> storageServices;

    private Map<String, ConnectionService> connectionServices;

    private ImportFacade importFacade;

    private ExportFacade exportFacade;

    private TaskInfoService<TaskInfoModel> taskInfoService;

    private StorageConfigValidator storageConfigValidator;

    private ModelService modelService;

    private MediaService mediaService;

	 @Override
	 public void connect(final StorageConfigData configData) throws CloudStorageException
	 {
        final ConnectionService connectionService = obtainConnectionService(configData);

        connectionService.connect(configData);
    }

    @Override
    public String getAuthURL(final StorageConfigData configData, final HttpSession httpSession) throws CloudStorageException
    {
        final ConnectionService connectionService = obtainConnectionService(configData);

        return connectionService.getAuthURL(configData, httpSession);
    }

    @Override
    public void checkAccessToken(final StorageConfigData selectedConfigData) throws IemException
    {
        try
        {
            final ConnectionService connectionService = obtainConnectionService(selectedConfigData);

            connectionService.checkAccessToken(selectedConfigData);
        }
        catch (final InvalidTokenException e)
        {
            clearDataForInvalidToken(selectedConfigData);
            throw e;
        }
    }

    @Override
    public TaskInfoData download(final TaskInfoData taskInfoData) throws CloudStorageException
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug(String.format("Start downloading file from [%s]...", taskInfoData.getCloudFileDownloadPathToDisplay()));
        }
        final TaskInfoData result;
        final StorageConfigData storageConfigData = taskInfoData.getConfig();

        final StorageService storageService = obtainStorageService(storageConfigData);

        final ImportTaskInfoModel importTaskInfoModel = taskInfoService.createImportTaskInfoModel(taskInfoData);

        taskInfoData.setTaskInfoCode(importTaskInfoModel.getCode());

        try
        {
            result = storageService.download(taskInfoData);
            taskInfoService.setTaskInfoStatus(importTaskInfoModel, TaskInfoStatus.DOWNLOADED);
        }
        catch (final CloudStorageException e)
        {
            taskInfoService.setTaskInfoStatus(importTaskInfoModel, TaskInfoStatus.FAILED);
            throw e;
        }
        return result;
    }

    @Override
    public void disconnect(final StorageConfigData configData) throws CloudStorageException
    {
        final ConnectionService connectionService = obtainConnectionService(configData);
        connectionService.revokeToken(configData);
    }

    @Override
    public TaskInfoData importData(final TaskInfoData taskInfoData) throws ImportException
    {
        final TaskInfoData importedTaskInfoData = importFacade.importData(taskInfoData);
        removeTemporaryFile(importedTaskInfoData);
        return importedTaskInfoData;
    }

    @Override
    public TaskInfoData exportData(final TaskInfoData taskInfoData) throws ExportException
    {
        return exportFacade.exportData(taskInfoData);
    }

    @Override
    public TaskInfoData upload(final TaskInfoData taskInfoData) throws CloudStorageException {
        final StorageService storageService = obtainStorageService(taskInfoData.getConfig());

        return storageService.upload(taskInfoData);
    }

    @Override
    public Collection<CloudObjectData> listFiles(final TaskInfoData taskInfoData) throws CloudStorageException {
        final StorageService storageService = obtainStorageService(taskInfoData.getConfig());

        return storageService.listFiles(taskInfoData);
    }

    private ConnectionService obtainConnectionService(final StorageConfigData configData) throws CloudStorageException
    {
        storageConfigValidator.validate(configData);

        final String storageTypeCode = configData.getStorageTypeData().getCode();

        return connectionServices.get(storageTypeCode);
    }

    private StorageService obtainStorageService(final StorageConfigData configData) throws CloudStorageException
    {
        storageConfigValidator.validate(configData);

        final String storageTypeCode = configData.getStorageTypeData().getCode();

        return storageServices.get(storageTypeCode);
    }

    public Map<String, StorageService> getStorageServices()
    {
        return storageServices;
    }

    @Required
    public void setStorageServices(final Map<String, StorageService> storageServices)
    {
        this.storageServices = storageServices;
    }

    private void clearDataForInvalidToken(final StorageConfigData configData)
    {
        final TaskInfoData taskInfoData = new TaskInfoData();
        taskInfoData.setConfig(configData);
        configData.setIsConnected(Boolean.FALSE);
        configData.setAuthCode(StringUtils.EMPTY);
    }

    @Override
    public void removeTemporaryFile(final TaskInfoData taskInfoData)
    {
        final String exportedMediaCode = taskInfoData.getExportedMediaCode();
        final Path importTempFilePath = taskInfoData.getDownloadedFilePath();

        if (exportedMediaCode != null)
        {
            removeMedia(exportedMediaCode);
        }
        else if (importTempFilePath != null)
        {
            removeFile(importTempFilePath);
        }

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Removed temporary file");
        }
    }

    private void removeMedia(final String mediaCode)
    {
        final MediaModel media = mediaService.getMedia(mediaCode);
        try
        {
            mediaService.removeDataFromMedia(media);

            modelService.remove(media);
        }
        catch (final ModelRemovalException e)
        {
            LOGGER.error("Media with code " + media.getCode() + " could not be deleted: ", e);
        }
    }

    private void removeFile(final Path importTempFilePath)
    {
        try
        {
            Files.delete(importTempFilePath);
        }
        catch (final IOException e)
        {
            LOGGER.error("File with path " + importTempFilePath + " could not be deleted: ", e);
        }
    }

    public ImportFacade getImportFacade()
    {
        return importFacade;
    }

    @Required
    public void setImportFacade(final ImportFacade importFacade)
    {
        this.importFacade = importFacade;
    }

    public ExportFacade getExportFacade()
    {
        return exportFacade;
    }

    @Required
    public void setExportFacade(final ExportFacade exportFacade)
    {
        this.exportFacade = exportFacade;
    }

    public ModelService getModelService()
    {
        return modelService;
    }

    @Required
    public void setModelService(final ModelService modelService)
    {
        this.modelService = modelService;
    }

    @Required
    public void setMediaService(final MediaService mediaService)
    {
        this.mediaService = mediaService;
    }

    public TaskInfoService<TaskInfoModel> getTaskInfoService()
    {
        return taskInfoService;
    }

    @Required
    public void setTaskInfoService(final TaskInfoService<TaskInfoModel> taskInfoService)
    {
        this.taskInfoService = taskInfoService;
    }

    public Map<String, ConnectionService> getConnectionServices()
    {
        return connectionServices;
    }

    @Required
    public void setConnectionServices(final Map<String, ConnectionService> connectionServices)
    {
        this.connectionServices = connectionServices;
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
