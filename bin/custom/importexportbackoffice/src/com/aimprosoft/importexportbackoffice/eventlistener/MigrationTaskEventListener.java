package com.aimprosoft.importexportbackoffice.eventlistener;

import com.aimprosoft.importexportcloud.event.MigrationTaskEvent;
import de.hybris.platform.servicelayer.event.EventSender;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;


public class MigrationTaskEventListener extends AbstractEventListener<MigrationTaskEvent>
{

	@Override
	protected void onEvent(final MigrationTaskEvent migrationTaskEvent)
	{
		getBackofficeEventSender().sendEvent(migrationTaskEvent);
	}

	protected EventSender getBackofficeEventSender()
	{
		throw new UnsupportedOperationException(
				"Please define in the spring configuration a <lookup-method> for getBackofficeEventSender().");
	}
}
