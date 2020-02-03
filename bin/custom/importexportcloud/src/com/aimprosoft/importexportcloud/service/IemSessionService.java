package com.aimprosoft.importexportcloud.service;

import de.hybris.platform.servicelayer.session.SessionService;

import java.util.function.Supplier;


public interface IemSessionService extends SessionService
{
	/**
	 * Gets supplier results temporary disabling search restrictions.
	 *
	 * @param supplier supplier of results
	 * @param <T>      the type of results supplied by this supplier
	 * @return supplier results
	 */
	<T> T executeWithoutSearchRestrictions(Supplier<T> supplier);
}
