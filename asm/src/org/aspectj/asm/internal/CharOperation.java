/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version
 *******************************************************************/
package org.aspectj.asm.internal;

/**
 * Taken from org.aspectj.org.eclipse.jdt.core.compiler.CharOperation
 * 
 */
public class CharOperation {

	public static final char[][] NO_CHAR_CHAR = new char[0][];

	public static final char[] NO_CHAR = new char[0];

	/**
	 * Taken from org.aspectj.org.eclipse.jdt.core.compiler.CharOperation
	 */
	public static final char[] subarray(char[] array, int start, int end) {
		if (end == -1)
			end = array.length;
		if (start > end)
			return null;
		if (start < 0)
			return null;
		if (end > array.length)
			return null;

		char[] result = new char[end - start];
		System.arraycopy(array, start, result, 0, end - start);
		return result;
	}

	public static final char[][] subarray(char[][] array, int start, int end) {
		if (end == -1)
			end = array.length;
		if (start > end)
			return null;
		if (start < 0)
			return null;
		if (end > array.length)
			return null;

		char[][] result = new char[end - start][];
		System.arraycopy(array, start, result, 0, end - start);
		return result;
	}

	public static final char[][] splitOn(char divider, char[] array) {
		int length = array == null ? 0 : array.length;
		if (length == 0)
			return NO_CHAR_CHAR;

		int wordCount = 1;
		for (int i = 0; i < length; i++)
			if (array[i] == divider)
				wordCount++;
		char[][] split = new char[wordCount][];
		int last = 0, currentWord = 0;
		for (int i = 0; i < length; i++) {
			if (array[i] == divider) {
				split[currentWord] = new char[i - last];
				System.arraycopy(array, last, split[currentWord++], 0, i - last);
				last = i + 1;
			}
		}
		split[currentWord] = new char[length - last];
		System.arraycopy(array, last, split[currentWord], 0, length - last);
		return split;
	}

	/**
	 * Taken from org.aspectj.org.eclipse.jdt.core.compiler.CharOperation
	 */
	public static final int lastIndexOf(char toBeFound, char[] array) {
		for (int i = array.length; --i >= 0;)
			if (toBeFound == array[i])
				return i;
		return -1;
	}

	/**
	 * Taken from org.aspectj.org.eclipse.jdt.core.compiler.CharOperation
	 */
	public static final int indexOf(char toBeFound, char[] array) {
		for (int i = 0; i < array.length; i++)
			if (toBeFound == array[i])
				return i;
		return -1;
	}

	/**
	 * Taken from org.aspectj.org.eclipse.jdt.core.compiler.CharOperation
	 */
	public static final char[] concat(char[] first, char[] second) {
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
	 * Taken from org.aspectj.org.eclipse.jdt.core.compiler.CharOperation
	 */
	public static final boolean equals(char[] first, char[] second) {
		if (first == second)
			return true;
		if (first == null || second == null)
			return false;
		if (first.length != second.length)
			return false;

		for (int i = first.length; --i >= 0;)
			if (first[i] != second[i])
				return false;
		return true;
	}

	final static public String toString(char[][] array) {
		char[] result = concatWith(array, '.');
		return new String(result);
	}

	public static final char[] concatWith(char[][] array, char separator) {
		int length = array == null ? 0 : array.length;
		if (length == 0)
			return CharOperation.NO_CHAR;

		int size = length - 1;
		int index = length;
		while (--index >= 0) {
			if (array[index].length == 0)
				size--;
			else
				size += array[index].length;
		}
		if (size <= 0)
			return CharOperation.NO_CHAR;
		char[] result = new char[size];
		index = length;
		while (--index >= 0) {
			length = array[index].length;
			if (length > 0) {
				System.arraycopy(array[index], 0, result, (size -= length), length);
				if (--size >= 0)
					result[size] = separator;
			}
		}
		return result;
	}

	public static final int hashCode(char[] array) {
		int length = array.length;
		int hash = length == 0 ? 31 : array[0];
		if (length < 8) {
			for (int i = length; --i > 0;)
				hash = (hash * 31) + array[i];
		} else {
			// 8 characters is enough to compute a decent hash code, don't waste time examining every character
			for (int i = length - 1, last = i > 16 ? i - 16 : 0; i > last; i -= 2)
				hash = (hash * 31) + array[i];
		}
		return hash & 0x7FFFFFFF;
	}

	public static final boolean equals(char[][] first, char[][] second) {
		if (first == second)
			return true;
		if (first == null || second == null)
			return false;
		if (first.length != second.length)
			return false;

		for (int i = first.length; --i >= 0;)
			if (!equals(first[i], second[i]))
				return false;
		return true;
	}

	/**
	 * Taken from org.aspectj.org.eclipse.jdt.core.compiler.CharOperation
	 */
	public static final void replace(char[] array, char toBeReplaced, char replacementChar) {
		if (toBeReplaced != replacementChar) {
			for (int i = 0, max = array.length; i < max; i++) {
				if (array[i] == toBeReplaced)
					array[i] = replacementChar;
			}
		}
	}
}
