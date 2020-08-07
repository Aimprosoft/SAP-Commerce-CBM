package com.aimprosoft.importexportcloud.strategies.migration;

import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.cronjob.model.MediaFolderStructureMigrationCronJobModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;

import java.util.List;


public interface  IemMediaMigrationStrategy
{
	FlexibleSearchQuery createFetchQuery(MediaFolderStructureMigrationCronJobModel cjm);

	void process(List<MediaModel> elements);

}
