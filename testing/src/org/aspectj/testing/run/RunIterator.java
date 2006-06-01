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

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.LangUtil;

/**
 * Adapt IRun or Run[] or List or ListIterator to RunIteratorI.
 */
public class RunIterator implements IRunIterator {
    
    protected String name;
    protected ListIterator iter;
    protected IRun run;
    
    public RunIterator(String name, IRun run) {
        init(name, run);
    }

    public RunIterator(String name, List list) {
        init(name, list);
    }
    
    public RunIterator(String name, IRun[] runs) {
        init(name, Arrays.asList(runs).listIterator());
    }

    public RunIterator(String name, ListIterator iterator) {
        init(name, iterator);
    }

    public void init(String name, List list) {
        init(name, list.listIterator());
    }

    public void init(String name, IRun[] runs) {
        init(name, Arrays.asList(runs).listIterator());
    }
    
    /** @return true if the first IRun from nextRun can be the sole IRun */
    public boolean isHoistable() {
        return (null != run);
    }
    
    /** 
     * @param name if null, use iterator.toString();
     * @param iterator not null
     * @throws IllegalArgumentException if iterator is null
     */
    public void init(String name, ListIterator iterator) {
        LangUtil.throwIaxIfNull(iterator, "iterator");
        iter = iterator;
        name = (null != name? name : iterator.toString());
        run = null;
    }
    
    /** 
     * @param name if null, use run();
     * @param run not null
     * @throws IllegalArgumentException if iterator is null
     */
    public void init(String name, IRun run) {
        LangUtil.throwIaxIfNull(run, "run");
        this.run = run;
        name = (null != name? name : run.toString());
        iter = null;
    }

    /**
     * @return false always
	 * @see org.aspectj.testing.run.IRunIterator#abortOnFailure()
	 */
	public boolean abortOnFailure() {
		return false;
	}

    /**
	 * @see org.aspectj.testing.run.RunIteratorI#hasNextRun()
	 */
	public boolean hasNextRun() {
		return ((null != run) || ((null != iter) && (iter.hasNext())));
	}

    /**
	 * @see org.aspectj.testing.run.IRunIterator#iterationCompleted()
	 */
	public void iterationCompleted() {
	}

    
    /**
	 * @see org.aspectj.testing.run.RunIteratorI#nextRun(IMessageHandler, Runner)
	 */
	public IRun nextRun(IMessageHandler handler, Runner runner) {
		if (null != run) {
            IRun result = run;
            run = null;
            return result;
        }
        if (null != iter) {
            for (Object o = iter.next(); iter.hasNext();) {
                if (o instanceof IRunIterator) {
                    return runner.wrap((IRunIterator) o, null);
                } else if (o instanceof IRun) {
                    return (IRun) o;
                } else {
                    MessageUtil.error(handler, "not IRun or IRunIterator: " + o);
                }
            }
        }
        return null;            
	}

    /** @return name */
    public String toString() {
        return name;
    }
}
