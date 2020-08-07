package com.aimprosoft.importexportbackoffice.widgets.controllers;

import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;
import com.hybris.cockpitng.util.DefaultWidgetController;


public abstract class AbstractIemWidgetController extends DefaultWidgetController //NOSONAR
{

	private static final String CURRENT_TASK_INFO_DATA = "currentTaskInfo";

	protected TaskInfoData getOrCreateTaskInfoData()
	{
		TaskInfoData currentTaskInfoData = getWidgetInstanceManager().getModel()
				.getValue(CURRENT_TASK_INFO_DATA, TaskInfoData.class);

		if (currentTaskInfoData == null)
		{
			currentTaskInfoData = new TaskInfoData();
		}

		return currentTaskInfoData;
	}

	protected void updateTaskInfoDataForWidgetModel(final TaskInfoData taskInfoData)
	{
		getWidgetInstanceManager().getModel().put(CURRENT_TASK_INFO_DATA, taskInfoData);
	}

	protected String formatLabels(final String labelKey, final Object... values)
	{
		return String.format(getLabel(labelKey), values);
	}

	protected <T> T getValueFromWidgetModel(final String propertyName, final Class<T> className)
	{
		return getWidgetInstanceManager().getModel().getValue(propertyName, className);
	}

	protected void setValueToWidgetModel(final String propertyName, final Object value)
	{
		getWidgetInstanceManager().getModel().put(propertyName, value);
	}

}
