/* *******************************************************************
 * Copyright (c) 2003 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/


package org.aspectj.bridge;

/**
  * Used to give progress information typically to IDEs
  */
public interface IProgressListener {
	/**
	 * @param text the current phase of processing
	 */
	void setText(String text);

	/**
	 * @param percentDone how much work is completed so far
	 */
	void setProgress(double percentDone);

    /**
     * @param cancelRequested true if the caller wants the current compilation to stop asap
     */
	void setCancelledRequested(boolean cancelRequested);

	/**
	 * @return true if the consumer of the progress info would like the compileation to stop
	 */
	boolean isCancelledRequested();

}
