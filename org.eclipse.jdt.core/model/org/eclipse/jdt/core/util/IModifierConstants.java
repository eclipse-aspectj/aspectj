/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.util;

/**
 * Definition of the modifier constants as specified in the JVM specifications.
 *  
 * This interface is not intended to be implemented by clients. 
 * 
 * @since 2.0
 */
public interface IModifierConstants {

	int ACC_PUBLIC       = 0x0001;
	int ACC_PRIVATE      = 0x0002;
	int ACC_PROTECTED    = 0x0004;
	int ACC_STATIC       = 0x0008;
	int ACC_FINAL        = 0x0010;
	int ACC_SUPER        = 0x0020;
	int ACC_SYNCHRONIZED = 0x0020;
	int ACC_VOLATILE     = 0x0040;
	int ACC_TRANSIENT    = 0x0080;
	int ACC_NATIVE       = 0x0100;
	int ACC_INTERFACE    = 0x0200;
	int ACC_ABSTRACT     = 0x0400;
	int ACC_STRICT       = 0x0800;	
}
