/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajde;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.bridge.*;

/**
 * Used for displaying tasks, such as compiler messages, to the user.
 *
 * @author Mik Kersten
 */
public class NullIdeTaskListManager implements TaskListManager {
	
	List sourceLineTasks = new ArrayList();

    public void addSourcelineTask(String message, ISourceLocation sourceLocation, IMessage.Kind kind) {
    	sourceLineTasks.add(new SourceLineTask(message,sourceLocation,kind));
//    	System.out.println("> added sourceline task: " + message + ", file: " + sourceLocation.getSourceFile().getAbsolutePath()
//    		+ ": " +  sourceLocation.getLine());
    }
   
    public void addProjectTask(String message, IMessage.Kind kind) {
//    	System.out.println("> added project task: " + message + ", kind: " + kind);	
    }

    public void clearTasks() {
    	sourceLineTasks = new ArrayList();
//    	System.out.println("> cleared tasks");
    }
    
	/**
	 * Return the list of source line compiler messages resulting from a compile, so
	 * that we can test them.
	 * @return List
	 */
	public List getSourceLineTasks() {
		return sourceLineTasks;
	}


	public static class SourceLineTask {
		public String message;
		public ISourceLocation location;
		public IMessage.Kind kind;
		
		public SourceLineTask(String m,ISourceLocation l,IMessage.Kind k) {
			message = m;
			location = l;
			kind = k;
		}
	}
}
  
