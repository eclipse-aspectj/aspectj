/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors: IBM Corporation - initial API and implementation
 * 				 Helen Hawkins   - initial version (bug 148190)
 *******************************************************************/
package org.aspectj.ajde.core;

/**
 * Interface that presents the user with information about the
 * progress of the build
 */
public interface IBuildProgressMonitor {

	/**
	 * Start the progress monitor
	 */
	void begin();

	/**
	 * Sets the label describing the current progress phase.
	 */
	void setProgressText(String text);

	/**
	 * Stop the progress monitor
	 *
	 * @param wasFullBuild - true if was a full build, false otherwise
	 */
	void finish(boolean wasFullBuild);

	/**
	 * Sets the current progress done
	 *
	 * @param percentDone
	 */
	void setProgress(double percentDone);

	/**
	 * Checks whether the user has chosen to cancel the progress monitor
	 *
	 * @return true if progress monitor has been cancelled and false otherwise
	 */
	boolean isCancelRequested();

}
