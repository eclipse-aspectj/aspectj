/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
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
