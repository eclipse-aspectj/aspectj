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
	
}
