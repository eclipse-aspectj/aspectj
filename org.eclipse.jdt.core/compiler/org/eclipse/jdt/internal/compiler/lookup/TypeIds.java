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

public interface TypeIds {
	//base type void null undefined Object String
	//should have an id that is 0<= id <= 15

	final int T_undefined = 0; // should not be changed
	final int T_Object = 1;
	final int T_char = 2;
	final int T_byte = 3;
	final int T_short = 4;
	final int T_boolean = 5;
	final int T_void = 6;
	final int T_long = 7;
	final int T_double = 8;
	final int T_float = 9;
	final int T_int = 10;
	final int T_String = 11;
	final int T_null = 12;
	//final int T_extendedDouble = 13;
	//final int T_extendedLong = 14

	//=========end of 4 bits constraint===========

	final int T_JavaLangObject = T_Object; // for consistency
	final int T_JavaLangString = T_String; // for consistency

	// well-known exception types
	final int T_JavaLangClass = 16;
	final int T_JavaLangStringBuffer = 17;
	final int T_JavaLangSystem = 18;
	final int T_JavaLangError = 19;
	final int T_JavaLangReflectConstructor = 20;
	final int T_JavaLangThrowable = 21;
	final int T_JavaLangNoClassDefError = 22;
	final int T_JavaLangClassNotFoundException = 23;
	final int T_JavaIoPrintStream = 24;
	final int T_JavaLangException = 25;

	// wrapper types
	final int T_JavaLangByte = 26;
	final int T_JavaLangShort = 27;
	final int T_JavaLangCharacter = 28;
	final int T_JavaLangInteger = 29;
	final int T_JavaLangLong = 30;
	final int T_JavaLangFloat = 31;
	final int T_JavaLangDouble = 32;
	final int T_JavaLangBoolean = 33;
	final int T_JavaLangVoid = 34;

	// 1.4 feature
	final int T_JavaLangAssertionError = 35;
	final int NoId = Integer.MAX_VALUE;

	// implicit conversions: <compileType> to <runtimeType>  (note: booleans are integers at runtime)
	final int Boolean2Int = T_boolean + (T_int << 4);
	final int Boolean2String = T_boolean + (T_String << 4);
	final int Boolean2Boolean = T_boolean + (T_boolean << 4);
	final int Byte2Byte = T_byte + (T_byte << 4);
	final int Byte2Short = T_byte + (T_short << 4);
	final int Byte2Char = T_byte + (T_char << 4);
	final int Byte2Int = T_byte + (T_int << 4);
	final int Byte2Long = T_byte + (T_long << 4);
	final int Byte2Float = T_byte + (T_float << 4);
	final int Byte2Double = T_byte + (T_double << 4);
	final int Byte2String = T_byte + (T_String << 4);
	final int Short2Byte = T_short + (T_byte << 4);
	final int Short2Short = T_short + (T_short << 4);
	final int Short2Char = T_short + (T_char << 4);
	final int Short2Int = T_short + (T_int << 4);
	final int Short2Long = T_short + (T_long << 4);
	final int Short2Float = T_short + (T_float << 4);
	final int Short2Double = T_short + (T_double << 4);
	final int Short2String = T_short + (T_String << 4);
	final int Char2Byte = T_char + (T_byte << 4);
	final int Char2Short = T_char + (T_short << 4);
	final int Char2Char = T_char + (T_char << 4);
	final int Char2Int = T_char + (T_int << 4);
	final int Char2Long = T_char + (T_long << 4);
	final int Char2Float = T_char + (T_float << 4);
	final int Char2Double = T_char + (T_double << 4);
	final int Char2String = T_char + (T_String << 4);
	final int Int2Byte = T_int + (T_byte << 4);
	final int Int2Short = T_int + (T_short << 4);
	final int Int2Char = T_int + (T_char << 4);
	final int Int2Int = T_int + (T_int << 4);
	final int Int2Long = T_int + (T_long << 4);
	final int Int2Float = T_int + (T_float << 4);
	final int Int2Double = T_int + (T_double << 4);
	final int Int2String = T_int + (T_String << 4);
	final int Long2Byte = T_long + (T_byte << 4);
	final int Long2Short = T_long + (T_short << 4);
	final int Long2Char = T_long + (T_char << 4);
	final int Long2Int = T_long + (T_int << 4);
	final int Long2Long = T_long + (T_long << 4);
	final int Long2Float = T_long + (T_float << 4);
	final int Long2Double = T_long + (T_double << 4);
	final int Long2String = T_long + (T_String << 4);
	final int Float2Byte = T_float + (T_byte << 4);
	final int Float2Short = T_float + (T_short << 4);
	final int Float2Char = T_float + (T_char << 4);
	final int Float2Int = T_float + (T_int << 4);
	final int Float2Long = T_float + (T_long << 4);
	final int Float2Float = T_float + (T_float << 4);
	final int Float2Double = T_float + (T_double << 4);
	final int Float2String = T_float + (T_String << 4);
	final int Double2Byte = T_double + (T_byte << 4);
	final int Double2Short = T_double + (T_short << 4);
	final int Double2Char = T_double + (T_char << 4);
	final int Double2Int = T_double + (T_int << 4);
	final int Double2Long = T_double + (T_long << 4);
	final int Double2Float = T_double + (T_float << 4);
	final int Double2Double = T_double + (T_double << 4);
	final int Double2String = T_double + (T_String << 4);
	final int String2String = T_String + (T_String << 4);
	final int Object2String = T_Object + (T_String << 4);
	final int Null2String = T_null + (T_String << 4);
	final int Object2Object = T_Object + (T_Object << 4);
}
