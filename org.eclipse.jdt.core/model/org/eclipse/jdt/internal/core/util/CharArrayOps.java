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
package org.eclipse.jdt.internal.core.util;

/**
 * A class to do characters operations so that we can use
 * char arrays more effectively.
 */
public class CharArrayOps {
/**
 * Returns the char arrays as an array of Strings
 */
public static String[] charcharToString(char[][] charchar) {
	if (charchar == null) {
		return null;
	}
	String[] strings= new String[charchar.length];
	for (int i= 0; i < charchar.length; i++) {
		strings[i]= new String(charchar[i]);
	}
	return strings;
}
/**
 * Returns the char array as a String
 */
public static String charToString(char[] chars) {
	if (chars == null) {
		return null;
	} else {
		return new String(chars);
	}
}
/**
 * Concatinates the two arrays into one big array.
 * If the first array is null, returns the second array.
 * If the second array is null, returns the first array.
 *
 * @param first - the array which the other array is concatinated onto
 * @param second - the array which is to be concatinated onto the first array
 */
public static char[] concat(char[] first, char[] second) {
	if (first == null)
		return second;
	if (second == null)
		return first;

	int length1 = first.length;
	int length2 = second.length;
	char[] result = new char[length1 + length2];
	System.arraycopy(first, 0, result, 0, length1);
	System.arraycopy(second, 0, result, length1, length2);
	return result;
}
/**
 * Checks the two character arrays for equality.
 *
 * @param first - one of the arrays to be compared
 * @param second - the other array which is to be compared
 */
public static boolean equals(char[] first, char[] second) {
	if (first == second)
		return true;
	if (first == null || second == null)
		return false;
	if (first.length != second.length)
		return false;

	for (int i = 0, length = first.length; i < length; i++)
		if (first[i] != second[i])
			return false;
	return true;
}
/**
 * Returns the index of the first occurrence of character in buffer,
 * starting from offset, or -1 if not found.
 */
public static int indexOf(char character, char[] buffer, int offset) {
	for (int i= offset; i < buffer.length; i++) {
		if (buffer[i] == character) {
			return i;
		}
	}
	return -1;
}
/**
 * Extracts a sub-array from the given array, starting
 * at the given startIndex and proceeding for length characters.
 * Returns null if:
 *  1. the src array is null
 *  2. the start index is out of bounds
 *  3. the length parameter specifies a end point which is out of bounds
 * Does not return a copy of the array if possible, i.e. if start is zero
 * and length equals the length of the src array.
 *
 * @param src - the array from which elements need to be copied
 * @param start - the start index in the src array
 * @param length - the number of characters to copy
 */
public static char[] subarray(char[] src, int start, int length) {
	if (src == null)
		return null;
	int srcLength = src.length;
	if (start < 0 || start >= srcLength)
		return null;
	if (length < 0 || start + length > srcLength)
		return null;
	if (srcLength == length && start == 0)
		return src;
		
	char[] result = new char[length];
	if (length > 0)
		System.arraycopy(src, start, result, 0, length);
	return result;
}
/**
 * Extracts a substring from the given array, starting
 * at the given startIndex and proceeding for length characters.
 * Returns null if:
 *  1. the src array is null
 *  2. the start index is out of bounds
 *  3. the length parameter specifies a end point which is out of bounds
 * Does not return a copy of the array if possible, i.e. if start is zero
 * and length equals the length of the src array.
 *
 * @param src - the array from which elements need to be copied
 * @param start - the start index in the src array
 * @param length - the number of characters to copy
 */
public static String substring(char[] src, int start, int length) {
	char[] chars= subarray(src, start, length);
	if (chars != null) {
		return new String(chars);
	} else {
		return null;
	}
}
}
