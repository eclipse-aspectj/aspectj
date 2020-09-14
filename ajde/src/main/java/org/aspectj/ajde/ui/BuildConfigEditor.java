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


package org.aspectj.ajde.ui;

import java.io.IOException;

/**
 * @author Mik Kersten
 */
public interface BuildConfigEditor {

	/**
	 * @param filePath	the full path to the file resource to be opened
	 */
	void openFile(String filePath) throws IOException, InvalidResourceException;
}
