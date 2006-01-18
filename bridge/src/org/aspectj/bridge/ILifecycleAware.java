/* *******************************************************************
 * Copyright (c) 2006 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
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
