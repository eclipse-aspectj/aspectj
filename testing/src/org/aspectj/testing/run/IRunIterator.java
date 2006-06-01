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

/**
 * Iterator for IRun.
 * IRunIterator are useful if the underlying components
 * can use a generic IRunStatus and a single listener.
 * It is a requirement of any component runnin the IRunIterator
 * that they call iterationCompleted() when done, to permit
 * the IRunIterator to clean up.
 * @see Runner#runIterator(IRunIterator, IRunStatus, IRunListener)
 */
public interface IRunIterator {

    /** 
     * @return true if nextRun() would return something non-null 
     * @throws IllegalStateException if called after 
     *          <code>iterationCompleted()</code>
     */
    boolean hasNextRun();
    
    /**
     * Get the next run.
     * IRunIterator which contains child IRunIterator may either return
     * the children IRun or wrap them using
     * Runner.wrap(IRunIterator, IRunListener)
     * @param handler the IMessageHandler to use for error and other messages
     * @param runnere the Runner to use to wrap any nested IRunIterator as IRun. 
     * @return the next run, or null if there are no more.
     * @throws IllegalStateException if called after 
     *          <code>iterationCompleted()</code>
     * @see Runner#wrap(IRunIterator, IRunListener)
     */
    IRun nextRun(IMessageHandler handler, Runner runner);
    
    /**
     * Signal a runner that further runs should be aborted.  Runners
     * should check this after each failure.
     * @return true if the runner should stop iterating when an IRun fails
     * @throws IllegalStateException if called after 
     *          <code>iterationCompleted()</code>
     */
    boolean abortOnFailure(); // XXX supply IRun or IRunStatus?
    
    /** called when hasNextRun() and nextRun() will no longer be called */
    void iterationCompleted();
}
