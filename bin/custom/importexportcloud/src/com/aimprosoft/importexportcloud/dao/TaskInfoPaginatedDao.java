package com.aimprosoft.importexportcloud.dao;

import com.aimprosoft.importexportcloud.model.TaskInfoModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.paginated.dao.PaginatedGenericDao;


public interface TaskInfoPaginatedDao<T extends TaskInfoModel> extends PaginatedGenericDao<T>
{
	/**
	 * Gets specified task infos for user.
	 *
	 * @param userUid        user Uid to search
	 * @param type           instance of task info to search
	 * @param searchPageData pageable search data to identify the current pagination state
	 * @return task info search page data for given user and types
	 */
	SearchPageData<T> findByUserAndType(String userUid, T type, SearchPageData<T> searchPageData);
}
