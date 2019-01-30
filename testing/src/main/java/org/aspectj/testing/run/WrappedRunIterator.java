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

import org.aspectj.bridge.IMessageHandler;
import org.aspectj.util.LangUtil;

/** Adapt IRun to IRunIterator in a way that can be detected for hoisting. */
public class WrappedRunIterator implements IRunIterator {
    protected final Object id;
    protected IRun run;
    
    /**
     * @param id the Object used for toString(), if set
     * @param run the IRun returned from the first call to 
     * nextRun(IMessageHandler handler, Runner runner)
     */
    public WrappedRunIterator(Object id, IRun run) {
        LangUtil.throwIaxIfNull(run, "run");
        this.id = id;
        this.run = run;
    }

    /** @return false always - we run only once anyway */
    public boolean abortOnFailure() {
        return false;
    }

    /**
     * @return true until nextRun() completes
     * @see org.aspectj.testing.run.RunIteratorI#hasNextRun()
     */
    public boolean hasNextRun() {
        return (null != run);
    }

    /**
	 * @see org.aspectj.testing.run.IRunIterator#iterationCompleted()
	 */
	public void iterationCompleted() {
	}

    /**
     * @return the only IRun we have, and null thereafter
     * @see org.aspectj.testing.run.RunIteratorI#nextRun(IMessageHandler, Runner)
     */
    public IRun nextRun(IMessageHandler handler, Runner runner) {
        if (null == run) {
            return null;
        } else {
            IRun result = run;
            run = null;
            return result;
        }
    }

    /** @return name */
    public String toString() {
        return (null == id ? run : id).toString();
    }        
}
