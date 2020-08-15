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


package org.aspectj.testing.run;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Aggregate listeners into one and run synchronously in order added.
 * @author isberg
 */
public class RunListeners extends RunListener implements IRunListener {

    List listeners;
    public RunListeners() {
        listeners = new ArrayList();
    }
    
    public void addListener(IRunListener listener) {
        if (null != listener) {
            listeners.add(listener);
        }
    }

    public void removeListener(IRunListener listener) {
        if (null != listener) {
            listeners.remove(listener);
        }
    }
    

    /**
     * Run all listeners with the given status.
     * @see org.aspectj.testing.harness.run.IRunListener#runStarting(IRunStatus)
     */
    public final void runStarting(IRunStatus status) {
        if (null == status) {
            throw new IllegalArgumentException("null RunStatusI");
        }
        Iterator iter = listeners.iterator(); 
        while(!status.aborted() && iter.hasNext()) {
            IRunListener element = (IRunListener) iter.next();
            element.runStarting(status);
        }
    }

    /**
     * Signal all listeners with the given status.
     * @see org.aspectj.testing.harness.run.IRunListener#runCompleted(IRunStatus)
     */
    public final void runCompleted(IRunStatus status) {
        if (null == status) {
            throw new IllegalArgumentException("null RunStatusI");
        }
        Iterator iter = listeners.iterator(); 
        while(!status.aborted() && iter.hasNext()) {
            IRunListener element = (IRunListener) iter.next();
            element.runCompleted(status);
        }
    }
	/**
	 * @see org.aspectj.testing.harness.run.IRunListener#addingChild(IRunStatus, IRunStatus)
	 */
	public final void addingChild(IRunStatus parent, IRunStatus child) {
        if (null == child) {
            throw new IllegalArgumentException("null child");
        }
        if (null == parent) {
            throw new IllegalArgumentException("null parent");
        }
        Iterator iter = listeners.iterator(); 
        while(!parent.aborted() && ! child.aborted() && iter.hasNext()) {
            IRunListener element = (IRunListener) iter.next();
            element.addingChild(parent, child);
        }
	}
}
