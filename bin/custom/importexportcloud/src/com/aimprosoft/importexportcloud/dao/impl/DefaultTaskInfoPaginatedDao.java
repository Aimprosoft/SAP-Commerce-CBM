package com.aimprosoft.importexportcloud.dao.impl;

import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.dao.impl.DefaultPaginatedGenericDao;

import java.util.List;
import java.util.Map;

import com.aimprosoft.importexportcloud.dao.TaskInfoPaginatedDao;
import com.aimprosoft.importexportcloud.enums.TaskInfoStatus;
import com.aimprosoft.importexportcloud.model.TaskInfoModel;


public class DefaultTaskInfoPaginatedDao<T extends TaskInfoModel> extends DefaultPaginatedGenericDao<T>
		implements TaskInfoPaginatedDao<T>
{
	private static final String TYPECODE_PARAM = "typecode";
	private static final String USER_UID_PARAM = "userUid";

	private static final String TASK_INFO_BY_USER_AND_TYPE_QUERY = "SELECT {A." + TaskInfoModel.PK
			+ "} FROM { ?" + TYPECODE_PARAM + " AS A JOIN " + UserModel._TYPECODE + " AS B "
			+ " ON {A." + TaskInfoModel.USER + "} = {B." + UserModel.PK + "}"
			+ "} WHERE {B." + UserModel.UID + "} = ?" + USER_UID_PARAM;

	private static final String ACTIVE_TASK_INFO_QUERY = "SELECT {" + TaskInfoModel.PK
			+ "} FROM {" + TaskInfoModel._TYPECODE + " }"
			+ " WHERE {" + TaskInfoModel._TYPECODE + "." + TaskInfoModel.STATUS + "} NOT IN (?statuses)";

	private List<TaskInfoStatus> taskStatuses;

	public DefaultTaskInfoPaginatedDao(final String typeCode)
	{
		super(typeCode);
	}

	@Override
	public SearchPageData<T> findByUserAndType(final String userUid, final TaskInfoModel type, final SearchPageData searchPageData)
	{
		final FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(getTaskInfoQuery(type));
		flexibleSearchQuery.addQueryParameter(USER_UID_PARAM, userUid);
		flexibleSearchQuery.setDisableCaching(Boolean.TRUE);
		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
		parameter.setFlexibleSearchQuery(flexibleSearchQuery);
		parameter.setSearchPageData(searchPageData);
		return getPaginatedFlexibleSearchService().search(parameter);
	}

	public SearchPageData<T> findActiveTasks(final SearchPageData searchPageData)
	{
		final FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(ACTIVE_TASK_INFO_QUERY);
		final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();

		flexibleSearchQuery.setDisableCaching(Boolean.TRUE);
		flexibleSearchQuery.addQueryParameter("statuses", getTaskStatuses());
		parameter.setFlexibleSearchQuery(flexibleSearchQuery);
		parameter.setSearchPageData(searchPageData);
		return getPaginatedFlexibleSearchService().search(parameter);
	}

	@Override
	protected FlexibleSearchQuery createFlexibleSearchQuery(final Map<String, ?> params)
	{
		final FlexibleSearchQuery flexibleSearchQuery = super.createFlexibleSearchQuery(params);
		flexibleSearchQuery.setDisableCaching(Boolean.TRUE);
		return flexibleSearchQuery;
	}

	private String getTaskInfoQuery(final TaskInfoModel type)
	{
		return TASK_INFO_BY_USER_AND_TYPE_QUERY.replace("?" + TYPECODE_PARAM, type.getItemtype());
	}

	public List<TaskInfoStatus> getTaskStatuses()
	{
		return taskStatuses;
	}

	public void setTaskStatuses(final List<TaskInfoStatus> taskStatuses)
	{
		this.taskStatuses = taskStatuses;
	}
}
