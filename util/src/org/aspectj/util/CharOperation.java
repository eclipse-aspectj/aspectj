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
package org.aspectj.util;


/**
 * Taken from org.aspectj.org.eclipse.jdt.core.compiler.CharOperation
 *
 */
public class CharOperation {
	
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
	public static final boolean contains(char character, char[] array) {
		for (int i = array.length; --i >= 0;)
			if (array[i] == character)
				return true;
		return false;
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
	
	/**
	 * Taken from org.aspectj.org.eclipse.jdt.core.compiler.CharOperation
	 */	
	public static final void replace(
		char[] array,
		char toBeReplaced,
		char replacementChar) {
		if (toBeReplaced != replacementChar) {
			for (int i = 0, max = array.length; i < max; i++) {
				if (array[i] == toBeReplaced)
					array[i] = replacementChar;
			}
		}
	}
}
