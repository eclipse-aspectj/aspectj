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
package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.internal.compiler.env.*;

public interface ClassFileConstants extends IConstants {
	
	int Utf8Tag = 1;
	int IntegerTag = 3;
	int FloatTag = 4;
	int LongTag = 5;
	int DoubleTag = 6;
	int ClassTag = 7;
	int StringTag = 8;
	int FieldRefTag = 9;
	int MethodRefTag = 10;
	int InterfaceMethodRefTag = 11;
	int NameAndTypeTag = 12;
	
	int ConstantMethodRefFixedSize = 5;
	int ConstantClassFixedSize = 3;
	int ConstantDoubleFixedSize = 9;
	int ConstantFieldRefFixedSize = 5;
	int ConstantFloatFixedSize = 5;
	int ConstantIntegerFixedSize = 5;
	int ConstantInterfaceMethodRefFixedSize = 5;
	int ConstantLongFixedSize = 9;
	int ConstantStringFixedSize = 3;
	int ConstantUtf8FixedSize = 3;
	int ConstantNameAndTypeFixedSize = 5;
}
