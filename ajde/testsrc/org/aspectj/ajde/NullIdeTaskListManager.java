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
    boolean hasWarning = false;

    public void addSourcelineTask(
        String message,
        ISourceLocation sourceLocation,
        IMessage.Kind kind) {
        addSourcelineTask(new Message(message, kind, null, sourceLocation));
        if (!hasWarning && IMessage.WARNING.isSameOrLessThan(kind)) {
            hasWarning = true;
        }
    }
    
    public void addSourcelineTask(IMessage message) {
    	sourceLineTasks.add(new SourceLineTask(message));
        if (!hasWarning && IMessage.WARNING.isSameOrLessThan(message.getKind())) {
            hasWarning = true;
        }
//    	System.out.println("> added sourceline task: " + message + ", file: " + sourceLocation.getSourceFile().getAbsolutePath()
//    		+ ": " +  sourceLocation.getLine());
    }
   
    public void addProjectTask(String message, IMessage.Kind kind) {
        if (!hasWarning && IMessage.WARNING.isSameOrLessThan(kind)) {
            hasWarning = true;
        }
//    	System.out.println("> added project task: " + message + ", kind: " + kind);	
    }

    public boolean hasWarning() {
        return hasWarning;
    }
    
    public void clearTasks() {
    	sourceLineTasks = new ArrayList();
        hasWarning = false;
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
		
		public SourceLineTask(IMessage m) {
			message = m.getMessage();
			location = m.getISourceLocation();
			kind = m.getKind();
		}
        public String toString() {
            String loc = "<no location";
            if (null != location) {
                loc = location.getSourceFile() + ":" + location.getLine();
            }
            return "SourceLineTask [" + message 
                + ", " + loc
                + ", " + kind
                + "]";
        }
	}
}
  
