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
package org.eclipse.jdt.internal.compiler.util;

/**
 * Hashtable of {char[] --> Object }
 */
public final class HashtableOfObject implements Cloneable {
	
	// to avoid using Enumerations, walk the individual tables skipping nulls
	public char[] keyTable[];
	public Object valueTable[];

	public int elementSize; // number of elements in the table
	int threshold;

	public HashtableOfObject() {
		this(13);
	}

	public HashtableOfObject(int size) {

		this.elementSize = 0;
		this.threshold = size; // size represents the expected number of elements
		int extraRoom = (int) (size * 1.75f);
		if (this.threshold == extraRoom)
			extraRoom++;
		this.keyTable = new char[extraRoom][];
		this.valueTable = new Object[extraRoom];
	}

	public Object clone() throws CloneNotSupportedException {
		HashtableOfObject result = (HashtableOfObject) super.clone();
		result.elementSize = this.elementSize;
		result.threshold = this.threshold;

		int length = this.keyTable.length;
		result.keyTable = new char[length][];
		System.arraycopy(this.keyTable, 0, result.keyTable, 0, length);

		length = this.valueTable.length;
		result.valueTable = new Object[length];
		System.arraycopy(this.valueTable, 0, result.valueTable, 0, length);
		return result;
	}

	public boolean containsKey(char[] key) {

		int index = CharOperation.hashCode(key) % valueTable.length;
		int keyLength = key.length;
		char[] currentKey;
		while ((currentKey = keyTable[index]) != null) {
			if (currentKey.length == keyLength
				&& CharOperation.prefixEquals(currentKey, key))
				return true;
			index = (index + 1) % keyTable.length;
		}
		return false;
	}

	public Object get(char[] key) {

		int index = CharOperation.hashCode(key) % valueTable.length;
		int keyLength = key.length;
		char[] currentKey;
		while ((currentKey = keyTable[index]) != null) {
			if (currentKey.length == keyLength
				&& CharOperation.prefixEquals(currentKey, key))
				return valueTable[index];
			index = (index + 1) % keyTable.length;
		}
		return null;
	}

	public Object put(char[] key, Object value) {

		int index = CharOperation.hashCode(key) % valueTable.length;
		int keyLength = key.length;
		char[] currentKey;
		while ((currentKey = keyTable[index]) != null) {
			if (currentKey.length == keyLength
				&& CharOperation.prefixEquals(currentKey, key))
				return valueTable[index] = value;
			index = (index + 1) % keyTable.length;
		}
		keyTable[index] = key;
		valueTable[index] = value;

		// assumes the threshold is never equal to the size of the table
		if (++elementSize > threshold)
			rehash();
		return value;
	}

	public Object removeKey(char[] key) {

		int index = CharOperation.hashCode(key) % valueTable.length;
		int keyLength = key.length;
		char[] currentKey;
		while ((currentKey = keyTable[index]) != null) {
			if (currentKey.length == keyLength
				&& CharOperation.prefixEquals(currentKey, key)) {
					Object value = valueTable[index];
					elementSize--;
					keyTable[index] = null;
					valueTable[index] = null;
					rehash();
					return value;
				}
			index = (index + 1) % keyTable.length;
		}
		return null;
	}

	private void rehash() {

		HashtableOfObject newHashtable = new HashtableOfObject(elementSize * 2);		// double the number of expected elements
		char[] currentKey;
		for (int i = keyTable.length; --i >= 0;)
			if ((currentKey = keyTable[i]) != null)
				newHashtable.put(currentKey, valueTable[i]);

		this.keyTable = newHashtable.keyTable;
		this.valueTable = newHashtable.valueTable;
		this.threshold = newHashtable.threshold;
	}

	public int size() {
		return elementSize;
	}

	public String toString() {
		String s = ""; //$NON-NLS-1$
		Object object;
		for (int i = 0, length = valueTable.length; i < length; i++)
			if ((object = valueTable[i]) != null)
				s += new String(keyTable[i]) + " -> " + object.toString() + "\n"; 	//$NON-NLS-2$ //$NON-NLS-1$
		return s;
	}
}