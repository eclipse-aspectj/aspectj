/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver;

/**
 * Represents any element that may have annotations
 */
public interface AnnotatedElement {
	boolean hasAnnotation(TypeX ofType);
	
	// SomeType getAnnotation(TypeX ofType);
}
