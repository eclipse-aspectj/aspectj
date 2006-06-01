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


package org.aspectj.ajde;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;

/**
 * Used for displaying tasks, such as compiler messages, to the user.
 *
 * @author Mik Kersten
 */
public interface TaskListManager {

	/**
	 * Add a task associated with a particular line in a source file.
	 * 
	 * @param	message			description of compiler message
	 * @param	sourceLocation	can be null
	 * @param	kind			one of <code>ERROR_MESSAGE, WARNING_MESSAGE, INFO_MESSAGE</code>
	 */ 
    public void addSourcelineTask(String message, ISourceLocation sourceLocation, IMessage.Kind kind);

    public void addSourcelineTask(IMessage message);

    /** @return true if any messages in list have kind warning or greater */
    public boolean hasWarning();
    
	/**
	 * Add a task associated with the current project.
	 * 
	 * @param	message			description of compiler message
	 * @param	kind			one of <code>ERROR_MESSAGE, WARNING_MESSAGE, INFO_MESSAGE</code>
	 */     
    public void addProjectTask(String message, IMessage.Kind kind);
    
    /**
     * Delete all of the currently active tasks.
     */ 
    public void clearTasks();    
}

