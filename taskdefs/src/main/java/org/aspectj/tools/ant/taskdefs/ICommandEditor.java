/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Wes Isberg     initial implementation
 * ******************************************************************/

package org.aspectj.tools.ant.taskdefs;

/**
 * Preprocess command-lines
 * @author Wes Isberg
 */
public interface ICommandEditor {
    /**
     * Edit command being used.
     * @param command the String[] to edit
     * @return String[] input command if unchanged,
     *         or new non-null array of non-null components otherwise
     */
    String[] editCommand(String[] command);
}
