/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/


package org.aspectj.testing.run;

/**
 * Listen to events in the run lifecycle -
 * birth, death, and procreation.
 * @author isberg
 */
public interface IRunListener {

    /**
     * Called when run is about to be started.
     */
    void runStarting(IRunStatus run);

    /**
     * Called when run is has completed.
     */
    void runCompleted(IRunStatus run);

    /**
     * Called when adding a child to a parent run
     */
    void addingChild(IRunStatus parent, IRunStatus child);
}
