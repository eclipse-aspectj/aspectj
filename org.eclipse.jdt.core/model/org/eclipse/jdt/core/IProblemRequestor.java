/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core;

import org.eclipse.jdt.core.compiler.IProblem;

/**
 * A callback interface for receiving java problem as they are discovered
 * by some Java operation.
 * 
 * @see IProblem
 * @since 2.0
 */
public interface IProblemRequestor {
	
	/**
	 * Notification of a Java problem.
	 * 
	 * @param problem IProblem - The discovered Java problem.
	 */	
	void acceptProblem(IProblem problem);

	/**
	 * Notification sent before starting the problem detection process.
	 * Typically, this would tell a problem collector to clear previously recorded problems.
	 */
	void beginReporting();

	/**
	 * Notification sent after having completed problem detection process.
	 * Typically, this would tell a problem collector that no more problems should be expected in this
	 * iteration.
	 */
	void endReporting();

	/**
	 * Predicate allowing the problem requestor to signal whether or not it is currently
	 * interested by problem reports. When answering <code>false</false>, problem will
	 * not be discovered any more until the next iteration.
	 * 
	 * This  predicate will be invoked once prior to each problem detection iteration.
	 * 
	 * @return boolean - indicates whether the requestor is currently interested by problems.
	 */
	boolean isActive();
}
