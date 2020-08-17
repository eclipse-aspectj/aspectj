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

/**
 * A run is a Runnable that may know how to set its own status.
 * @author isberg
 */
public interface IRun {
    IRun[] RA_IRun = new IRun[0];

    /** Positive wrapper for the status parameter */
	IRun OK
        = new IRun() {
                /** This returns false when the status is null
                 * or runResult is false */
                public boolean run(IRunStatus status) {
                    return ((null != status) && status.runResult());
                }
                public IRunStatus makeStatus() { return null; }
            };

    /** Negative wrapper for the status parameter */
	IRun NOTOK
        = new IRun() {
                public boolean run(IRunStatus status) {
                    return ((null == status) || !status.runResult());
                }
                public IRunStatus makeStatus() { return null; }
            };

    /**
     * Run process, setting any known status.
     * Normally the caller starts the process
     * and the callee completes it, so that
     * status.isCompleted() returns true after
     * the call completes.  However, responsible
     * callees ensure starting, and responsible
     * callers ensure completed after the call.
     * Anyone setting completion should ensure it
     * is set recursively for all children,
     * and anyone starting child runs should
     * ensure children are registered and initialized
     * appropriately.
     * @param status the IRunStatus representing the
     *  outcome of the process (collecting parameter).
     * @see Runners
     */
    boolean run(IRunStatus status) throws Exception; // IMessageHandler?
}
