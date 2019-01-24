/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import org.aspectj.weaver.IHasPosition;

/**
 * Marker interface for BindingTypePattern and BindingAnnotationTypePattern
 */
public interface BindingPattern extends IHasPosition {
	int getFormalIndex();
}
