package com.aimprosoft.importexportcloud.facades.impl;

import com.aimprosoft.importexportcloud.facades.StorageConfigFacade;
import com.aimprosoft.importexportcloud.facades.data.StorageConfigData;
import com.aimprosoft.importexportcloud.facades.data.StorageTypeData;
import com.aimprosoft.importexportcloud.model.StorageConfigModel;
import com.aimprosoft.importexportcloud.service.StorageConfigService;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;


public class DefaultStorageConfigFacade implements StorageConfigFacade
{
	private StorageConfigService storageConfigService;

	private UserService userService;

	private ModelService modelService;

	private MediaService mediaService;

	private Map<String, Class<Object>> typeConfigClassMap;

	private Map<String, Converter<StorageConfigModel, StorageConfigData>> storageConfigConverters;

	private Map<String, Converter<StorageConfigData, StorageConfigModel>> storageConfigReverseConverters;

	@Override
	public List<StorageConfigData> getStorageConfigsDataByTypeCode(final String storageTypeCode)
	{
		final UserModel currentUser = userService.getCurrentUser();
		final List<StorageConfigModel> storageConfigs = storageConfigService
				.getAllStorageConfigsByUserAndTypeCode(currentUser, storageTypeCode);

		final Converter<StorageConfigModel, StorageConfigData> storageConfigConverter = storageConfigConverters
				.get(storageTypeCode);

		validateConverter(storageConfigConverter, storageTypeCode);

		return storageConfigConverter.convertAll(storageConfigs);
	}

	@Override
	public StorageConfigData getStorageConfigData(final String code)
	{
		final StorageConfigModel storageConfig = storageConfigService.getStorageConfigByCode(code);

		final String storageTypeCode = storageConfig.getType().getCode();
		final Converter<StorageConfigModel, StorageConfigData> storageConfigConverter = storageConfigConverters
				.get(storageTypeCode);

		validateConverter(storageConfigConverter, storageTypeCode);

		return storageConfigConverter.convert(storageConfig);
	}

	@Override
	public StorageConfigModel getStorageConfigModelByCode(final String code)
	{
		return storageConfigService.getStorageConfigByCode(code);
	}

	@Override
	public StorageConfigModel createStorageConfigModel(final String storageTypeCode)
	{
		final Class<?> modelClass = getTypeConfigClassMap().get(storageTypeCode);
		validateModelClass(modelClass, storageTypeCode);

		final Converter<StorageConfigData, StorageConfigModel> storageConfigReverseConverter = storageConfigReverseConverters
				.get(storageTypeCode);
		validateConverter(storageConfigReverseConverter, storageTypeCode);

		final StorageConfigModel result = modelService.create(modelClass);

		final StorageConfigData source = new StorageConfigData();
		final StorageTypeData storageTypeData = new StorageTypeData();
		storageTypeData.setCode(storageTypeCode);
		source.setStorageTypeData(storageTypeData);

		return storageConfigReverseConverter.convert(source, result);
	}

	private void validateConverter(final Converter converter, final String storageTypeCode)
	{
		validateParameterNotNull(converter,
				String.format("No storage config converter was found for storage type with code [%s].", storageTypeCode));
	}

	private void validateModelClass(final Class<?> modelClass, final String storageTypeCode)
	{
		validateParameterNotNull(modelClass,
				String.format("No storage config model class was found for storage type with code [%s].", storageTypeCode));
	}

	@Override
	public void removeStorageConfig(final String code)
	{
		final StorageConfigModel storageConfig = storageConfigService.getStorageConfigByCode(code);
		modelService.remove(storageConfig);
	}

	@Override
	public StorageConfigData getStorageConfigForFolder(final String folderQualifier, final Boolean withTargetCheck)
	{
		final MediaFolderModel mediaFolder = mediaService.getFolder(folderQualifier);
		final StorageConfigModel storageConfigModel = withTargetCheck ?
				getStorageConfigWithTargetCheck(mediaFolder) :
				mediaFolder.getCurrentStorageConfig();

		return storageConfigModel != null ? getConfigData(storageConfigModel) : null;
	}

	private StorageConfigModel getStorageConfigWithTargetCheck(final MediaFolderModel mediaFolder)
	{
		return mediaFolder.getTargetStorageConfig() != null ?
				mediaFolder.getTargetStorageConfig() :
				mediaFolder.getCurrentStorageConfig();
	}

	@Override
	public void setStorageConfigForFolder(final MediaFolderModel folder, final StorageConfigData storageConfig)
	{
		final Optional<StorageConfigModel> storageConfigOptional = Optional.ofNullable(storageConfig)
				.map(StorageConfigData::getCode)
				.map(this::getStorageConfigModelByCode);

		if (storageConfigOptional.isPresent())
		{
			folder.setTargetStorageConfig(storageConfigOptional.get());
		}
	}

	private StorageConfigData getConfigData(final StorageConfigModel storageConfigModel)
	{
		final String storageTypeCode = storageConfigModel.getType().getCode();
		final Converter<StorageConfigModel, StorageConfigData> converter = storageConfigConverters.get(storageTypeCode);
		validateConverter(converter, storageTypeCode);
		return converter.convert(storageConfigModel);
	}

	@Required
	public void setStorageConfigService(final StorageConfigService storageConfigService)
	{
		this.storageConfigService = storageConfigService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	@Required
	public void setStorageConfigConverters(
			final Map<String, Converter<StorageConfigModel, StorageConfigData>> storageConfigConverters)
	{
		this.storageConfigConverters = storageConfigConverters;
	}

	public MediaService getMediaService()
	{
		return mediaService;
	}

	@Required
	public void setMediaService(final MediaService mediaService)
	{
		this.mediaService = mediaService;
	}

	@Required
	public void setStorageConfigReverseConverters(
			final Map<String, Converter<StorageConfigData, StorageConfigModel>> storageConfigReverseConverters)
	{
		this.storageConfigReverseConverters = storageConfigReverseConverters;
	}

	public Map<String, Class<Object>> getTypeConfigClassMap()
	{
		return typeConfigClassMap;
	}

	@Required
	public void setTypeConfigClassMap(final Map<String, Class<Object>> typeConfigClassMap)
	{
		this.typeConfigClassMap = typeConfigClassMap;
	}
}
