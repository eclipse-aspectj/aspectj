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
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public interface CompilerModifiers extends ClassFileConstants { // modifier constant
	// those constants are depending upon ClassFileConstants (relying that classfiles only use the 16 lower bits)
	final int AccDefault = 0;
	final int AccJustFlag = 0xFFFF;
	final int AccCatchesExceptions = 0x10000;
	final int AccThrowsExceptions = 0x20000;
	final int AccProblem = 0x40000;
	final int AccFromClassFile = 0x80000;
	final int AccIsConstantValue = 0x80000;	
	final int AccDefaultAbstract = 0x80000;
	final int AccDeprecatedImplicitly = 0x200000; // ie. is deprecated itself or contained by a deprecated type
	final int AccAlternateModifierProblem = 0x400000;
	final int AccModifierProblem = 0x800000;
	final int AccSemicolonBody = 0x1000000;
	final int AccUnresolved = 0x2000000;
	final int AccClearPrivateModifier = 0x4000000; // might be requested during private access emulation
	final int AccVisibilityMASK = AccPublic | AccProtected | AccPrivate;
}
