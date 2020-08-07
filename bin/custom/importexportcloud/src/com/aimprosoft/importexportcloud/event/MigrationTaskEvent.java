package com.aimprosoft.importexportcloud.event;

import com.aimprosoft.importexportcloud.model.MigrationTaskInfoModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;


public class MigrationTaskEvent extends AbstractEvent
{
	private MigrationTaskInfoModel migrationTask;
	private UserModel user;

	public MigrationTaskEvent(final MigrationTaskInfoModel migrationTask, final UserModel user)
	{
		this.migrationTask = migrationTask;
		this.user = user;
	}

	public MigrationTaskInfoModel getMigrationTask()
	{
		return migrationTask;
	}

	public UserModel getUser()
	{
		return user;
	}
}
