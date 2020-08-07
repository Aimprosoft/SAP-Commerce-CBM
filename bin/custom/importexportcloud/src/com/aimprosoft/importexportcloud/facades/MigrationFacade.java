package com.aimprosoft.importexportcloud.facades;

import com.aimprosoft.importexportcloud.exceptions.MigrationException;
import com.aimprosoft.importexportcloud.facades.data.TaskInfoData;


public interface MigrationFacade
{
	/**
	 * Run migration cron job
	 *
	 * @param taskInfoData task information DTO
	 * @return task information DTO
	 * @throws MigrationException if migration process fails
	 */
	TaskInfoData runMigrationCronJob(TaskInfoData taskInfoData) throws MigrationException;
}
