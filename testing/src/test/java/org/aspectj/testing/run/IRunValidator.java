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
 * These check whether particular runs have passed.
 * @author isberg
 */
public interface IRunValidator {
    /**
     * Evaluate whether a run has passed.
     * @param run the IRunStatus to see if it passed.
     * @return true if run has passed
     */
    boolean runPassed(IRunStatus run);
}
