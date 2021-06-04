/* *******************************************************************
 * Copyright (c) 2006 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.bridge;

/**
 * Interface that can be implemented by MessageHandlers that need to
 * perform some additional processing when a build is starting and
 * when it has finished.
 *
 * @author Adrian Colyer
 * @since 1.5.1
 */
public interface ILifecycleAware {

	/**
	 * called when a build starts
	 */
	void buildStarting(boolean isIncremental);

	/**
	 * called when a batch build finishes
	 *
	 */
	void buildFinished(boolean wasIncremental);

}
