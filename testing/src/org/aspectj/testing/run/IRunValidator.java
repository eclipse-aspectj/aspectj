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
