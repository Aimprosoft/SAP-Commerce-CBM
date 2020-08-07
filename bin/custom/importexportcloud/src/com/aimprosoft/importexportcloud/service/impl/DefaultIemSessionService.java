package com.aimprosoft.importexportcloud.service.impl;

import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.session.impl.DefaultSessionService;

import java.util.function.Supplier;

import com.aimprosoft.importexportcloud.service.IemSessionService;


public class DefaultIemSessionService extends DefaultSessionService implements IemSessionService
{
	@Override
	public <T> T executeWithoutSearchRestrictions(final Supplier<T> supplier)
	{
		return ((SessionService) this).executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public Object execute()
			{
				try
				{
					setAttribute("disableRestrictions", Boolean.TRUE);
					return supplier.get();
				}
				finally
				{
					setAttribute("disableRestrictions", Boolean.FALSE);
				}
			}
		});
	}
}
