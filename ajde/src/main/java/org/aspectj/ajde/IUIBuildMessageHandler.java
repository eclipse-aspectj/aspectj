/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors: IBM Corporation - initial API and implementation
 * 				 Helen Hawkins   - initial version (bug 148190)
 *******************************************************************/
package org.aspectj.ajde;

import org.aspectj.ajde.core.IBuildMessageHandler;

/**
 * Extension to the IBuildMessageHandler to be used if only one BuildMessageHandler
 * is going to be used for all projects/build configuration files. Provides a method
 * for resetting the state of the BuildMessageHandler between compiles
 */
public interface IUIBuildMessageHandler extends IBuildMessageHandler {

	/**
	 * Reset the state of the message handler
	 */
	void reset();

}
