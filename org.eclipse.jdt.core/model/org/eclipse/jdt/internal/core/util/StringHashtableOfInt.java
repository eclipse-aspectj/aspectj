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
 * A hash table keyed by strings and with int values.
 */
public class StringHashtableOfInt {
	// to avoid using Enumerations, walk the individual tables skipping nulls
	public String[] keyTable;
	public int[] valueTable;

	int elementSize; // number of elements in the table
	int threshold;
public StringHashtableOfInt() {
	this(13);
}
public StringHashtableOfInt(int size) {
	this.elementSize = 0;
	this.threshold = size; // size represents the expected number of elements
	int extraRoom = (int) (size * 1.75f);
	if (this.threshold == extraRoom)
		extraRoom++;
	this.keyTable = new String[extraRoom];
	this.valueTable = new int[extraRoom];
}
public boolean containsKey(String key) {
	int index = (key.hashCode() & 0x7FFFFFFF) % valueTable.length;
	String currentKey;
	while ((currentKey = keyTable[index]) != null) {
		if (currentKey.equals(key))
			return true;
		index = (index + 1) % keyTable.length;
	}
	return false;
}
/**
 * Returns the value at the given key.
 * Returns -1 if not found.
 */
public int get(String key) {
	int index = (key.hashCode() & 0x7FFFFFFF) % valueTable.length;
	String currentKey;
	while ((currentKey = keyTable[index]) != null) {
		if (currentKey.equals(key))
			return valueTable[index];
		index = (index + 1) % keyTable.length;
	}
	return -1;
}
public int put(String key, int value) {
	int index = (key.hashCode() & 0x7FFFFFFF) % valueTable.length;
	String currentKey;
	while ((currentKey = keyTable[index]) != null) {
		if (currentKey.equals(key))
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
private void rehash() {
	StringHashtableOfInt newHashtable = new StringHashtableOfInt(elementSize * 2); // double the number of expected elements
	String currentKey;
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
/**
 * Return the keys sorted by their values.
 */
public String[] sortedKeys(int maxValue) {
	String[] result = new String[this.elementSize];
	
	// compute a list of the end positions of each layer in result
	int[] endPos = new int[maxValue+1];
	int length = this.keyTable.length;
	for (int i = 0; i < length; i++) {
		String key = this.keyTable[i];
		if (key != null) {
			for (int j = this.valueTable[i]; j <= maxValue; j++) {
				endPos[j]++;
			}
		}
	}

	// store the keys in order of their values
	for (int i = 0; i < length; i++) {
		String key = this.keyTable[i];
		if (key != null) {
			int value = this.valueTable[i];
			int index = --endPos[value];
			result[index] = key;
		}
	}

	return result;
}
public String toString() {
	StringBuffer buffer = new StringBuffer();
	for (int i = 0, length = this.valueTable.length; i < length; i++) {
		String key = this.keyTable[i];
		if (key != null) {
			buffer.append(key);
			buffer.append(" -> "); //$NON-NLS-1$
			buffer.append(this.valueTable[i]);
			buffer.append("\n"); //$NON-NLS-1$
		}
	}
	return buffer.toString();
}
}
