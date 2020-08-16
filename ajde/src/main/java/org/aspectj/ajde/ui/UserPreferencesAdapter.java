/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/



package org.aspectj.ajde.ui;

import java.util.List;

/**
 * This interface needs to be implemented by an IDE extension in order for AJDE
 * to store properties in a way that matches the IDE's property storing facilities.
 *
 * @author	Mik Kersten
 */
public interface UserPreferencesAdapter {

	/**
	 * Retrieves a global IDE option.
	 */
	String getGlobalPreference(String name);

	/**
	 * Retrieves a global IDE option.
	 */
	List getGlobalMultivalPreference(String name);

	/**
	 * Sets a global IDE option with a single value.
	 */
	void setGlobalPreference(String name, String value);

	/**
	 * Sets a global IDE option with multiple values.
	 */
	void setGlobalMultivalPreference(String name, List values);

	/**
	 * Retrieves an option for the currently active project.
	 */
	String getProjectPreference(String name);

	/**
	 * Retrieves an option for the currently active project.
	 */
	List getProjectMultivalPreference(String name);

	/**
	 * Sets an option for the currently active project.
	 */
	void setProjectPreference(String name, String value);

	/**
	 * Sets an option for the currently active project.
	 */
	void setProjectMultivalPreference(String name, List values);
}
