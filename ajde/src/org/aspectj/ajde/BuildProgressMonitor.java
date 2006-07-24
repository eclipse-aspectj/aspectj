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



package org.aspectj.ajde;

/**
 * This interface should be implemented by a progress monitor that that presents
 * the user with the current state of the compile and estimated finish.
 *
 * @author  Mik Kersten
 */
public interface BuildProgressMonitor {

	/**
	 * The heading that should be used in the title of the progress monitor. 
	 */ 
	public static final String PROGRESS_HEADING = "AspectJ Build";

    /**
     * Start the progress monitor.
     */
    public void start(String configFile);

	/**
	 * Sets the label describing the current progress phase.
	 */
    public void setProgressText(String text);

    /**
     * Jumps the progress bar to <CODE>newVal</CODE>.
     */
    public void setProgressBarVal(int newVal);

    /**
     * Makes the progress bar by one.
     */
    public void incrementProgressBarVal();

	/**
	 * @param	maxVal	sets the value at which the progress will finish.
	 */
    public void setProgressBarMax(int maxVal);

	/**
	 * @return	the value at which the progress monitoring will finish.
	 */
    public int getProgressBarMax();

    /**
     * Jump the progress bar to the end and finish progress monitoring.
     */
    public void finish(boolean wasFullBuild);
}
