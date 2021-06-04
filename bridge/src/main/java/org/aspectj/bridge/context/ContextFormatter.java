/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.bridge.context;

/**
 * @author colyer
 * Implementors of this interface know how to turn the "Object" data and phase id
 * associated with a context stack entry into a meaningful string.
 */
public interface ContextFormatter {
	String formatEntry(int phaseId, Object data);
}
