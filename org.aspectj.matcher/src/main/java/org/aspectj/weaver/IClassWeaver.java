/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/

package org.aspectj.weaver;

/**
 * An IClassWeaver is initialized with a class (a type, really, but let's ignore that for now) and a world, and has one method that
 * actually weaves the contents of the world into the class implementation.
 */

public interface IClassWeaver {

	/**
	 * perform the weaving.
	 *
	 * @return <code>true</code> if the class is changed by the weaving, <code>false</code> otherwise.
	 */
	boolean weave();
}
