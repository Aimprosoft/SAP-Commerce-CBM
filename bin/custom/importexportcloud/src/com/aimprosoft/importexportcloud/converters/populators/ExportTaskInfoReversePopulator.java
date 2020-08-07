package com.aimprosoft.importexportcloud.converters.populators;

import static com.aimprosoft.importexportcloud.constants.ImportexportcloudConstants.IEM_TRANSMIT_FILE_EXTENSION;
import static com.aimprosoft.importexportcloud.constants.ImportexportcloudConstants.STORAGE_PATH_SEPARATOR;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.aimprosoft.importexportcloud.enums.TaskInfoScope;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.aimprosoft.importexportcloud.model.ExportTaskInfoModel;
import com.aimprosoft.importexportcloud.service.IemCMSSiteService;
import com.aimprosoft.importexportcloud.service.IemCatalogVersionService;


public class ExportTaskInfoReversePopulator implements Populator<TaskInfoData, ExportTaskInfoModel>
{

	private static final String DEFAULT_EXPORT_FILE_PREFIX = "export-";

	private IemCMSSiteService iemCmsSiteService;

	private IemCatalogVersionService iemCatalogVersionService;

	private EnumerationService enumerationService;

	@Override
	public void populate(final TaskInfoData taskInfoData, final ExportTaskInfoModel exportTaskInfoModel) throws ConversionException
	{
		if (taskInfoData != null)
		{
			final CMSSiteModel cmsSiteModel = iemCmsSiteService.getCMSSiteForUid(taskInfoData.getCmsSiteUid());

			final CatalogVersionModel catalogVersionModel = iemCatalogVersionService
					.getCatalogVersionModelWithoutSearchRestrictions(taskInfoData.getCatalogIdAndVersionName());

			final TaskInfoScope taskInfoScope = enumerationService
					.getEnumerationValue(TaskInfoScope.class, taskInfoData.getTaskInfoScopeCode());

			validateSourceParameters(taskInfoScope, cmsSiteModel, catalogVersionModel);

			exportTaskInfoModel.setSite(cmsSiteModel);
			exportTaskInfoModel.setCatalogVersion(catalogVersionModel);

			final String externalDisplayPath = getExternalPath(taskInfoData.getCloudUploadFolderPathToDisplay(),
					taskInfoData.getResultPrefix(), exportTaskInfoModel.getCode());

			exportTaskInfoModel.setExternalPath(externalDisplayPath);
			exportTaskInfoModel.setTaskScope(taskInfoScope);
			exportTaskInfoModel.setMigrateMediaNeeded(taskInfoData.isExportMediaNeeded());
		}
	}

	private String getExternalPath(final String folderPath, final String resultPrefix, final String modelCode)
	{
		final String localFolderPath = folderPath.endsWith(STORAGE_PATH_SEPARATOR)
				? folderPath
				: folderPath + STORAGE_PATH_SEPARATOR;

		return localFolderPath + generateExportFileName(resultPrefix, modelCode);
	}

	private void validateSourceParameters(final TaskInfoScope taskInfoScope, final CMSSiteModel cmsSiteModel,
			final CatalogVersionModel catalogVersionModel)
	{
		if (taskInfoScope == null)
		{
			throw new ConversionException("Export task scope is not defined.");
		}

		if (taskInfoScope.equals(TaskInfoScope.SITESCOPE) && cmsSiteModel == null)
		{
			throw new ConversionException("Site is not defined in Export task with scope 'Site'");
		}

		if (taskInfoScope.equals(TaskInfoScope.CATALOGSCOPE) && catalogVersionModel == null)
		{
			throw new ConversionException("CatalogVersion is not defined in Export task with scope 'Catalog'");
		}
	}

	private String generateExportFileName(final String resultPrefix, final String code)
	{
		final String prefix = StringUtils.isNotEmpty(resultPrefix) ? resultPrefix + "_" : DEFAULT_EXPORT_FILE_PREFIX;
		return prefix + code + IEM_TRANSMIT_FILE_EXTENSION;
	}

	public IemCMSSiteService getIemCmsSiteService()
	{
		return iemCmsSiteService;
	}

	@Required
	public void setIemCmsSiteService(final IemCMSSiteService iemCmsSiteService)
	{
		this.iemCmsSiteService = iemCmsSiteService;
	}

	public IemCatalogVersionService getIemCatalogVersionService()
	{
		return iemCatalogVersionService;
	}

	@Required
	public void setIemCatalogVersionService(final IemCatalogVersionService iemCatalogVersionService)
	{
		this.iemCatalogVersionService = iemCatalogVersionService;
	}

	public EnumerationService getEnumerationService()
	{
		return enumerationService;
	}

	@Required
	public void setEnumerationService(final EnumerationService enumerationService)
	{
		this.enumerationService = enumerationService;
	}
}
