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
package org.eclipse.jdt.internal.compiler.codegen;

public class IntegerCache {
	public int keyTable[];
	public int valueTable[]; 
	int elementSize;
	int threshold;
/**
 * Constructs a new, empty hashtable. A default capacity and
 * load factor is used. Note that the hashtable will automatically 
 * grow when it gets full.
 */
public IntegerCache() {
	this(13);
}
/**
 * Constructs a new, empty hashtable with the specified initial
 * capacity.
 * @param initialCapacity int
 *  the initial number of buckets
 */
public IntegerCache(int initialCapacity) {
	elementSize = 0;
	threshold = (int) (initialCapacity * 0.66);
	keyTable = new int[initialCapacity];
	valueTable = new int[initialCapacity];
}
/**
 * Clears the hash table so that it has no more elements in it.
 */
public void clear() {
	for (int i = keyTable.length; --i >= 0;) {
		keyTable[i] = 0;
		valueTable[i] = 0;
	}
	elementSize = 0;
}
/** Returns true if the collection contains an element for the key.
 *
 * @param key <CODE>double</CODE> the key that we are looking for
 * @return boolean
 */
public boolean containsKey(int key) {
	int index = hash(key);
	while ((keyTable[index] != 0) || ((keyTable[index] == 0) &&(valueTable[index] != 0))) {
		if (keyTable[index] == key)
			return true;
		index = (index + 1) % keyTable.length;
	}
	return false;
}
/** Gets the object associated with the specified key in the
 * hashtable.
 * @param key <CODE>double</CODE> the specified key
 * @return int the element for the key or -1 if the key is not
 *  defined in the hash table.
 */
public int get(int key) {
	int index = hash(key);
	while ((keyTable[index] != 0) || ((keyTable[index] == 0) &&(valueTable[index] != 0))) {
		if (keyTable[index] == key)
			return valueTable[index];
		index = (index + 1) % keyTable.length;
	}
	return -1;
}
/**
 * Return a hashcode for the value of the key parameter.
 * @param key int
 * @return int the hash code corresponding to the key value
 */
public int hash(int key) {
	return (key & 0x7FFFFFFF) % keyTable.length;
}
/**
 * Puts the specified element into the hashtable, using the specified
 * key.  The element may be retrieved by doing a get() with the same key.
 * 
 * @param key <CODE>int</CODE> the specified key in the hashtable
 * @param value <CODE>int</CODE> the specified element
 * @return int value
 */
public int put(int key, int value) {
	int index = hash(key);
	while ((keyTable[index] != 0) || ((keyTable[index] == 0) && (valueTable[index] != 0))) {
		if (keyTable[index] == key)
			return valueTable[index] = value;
		index = (index + 1) % keyTable.length;
	}
	keyTable[index] = key;
	valueTable[index] = value;

	// assumes the threshold is never equal to the size of the table
	if (++elementSize > threshold) {
		rehash();
	}
	return value;
}
/**
 * Rehashes the content of the table into a bigger table.
 * This method is called automatically when the hashtable's
 * size exceeds the threshold.
 */
private void rehash() {
	IntegerCache newHashtable = new IntegerCache(keyTable.length * 2);
	for (int i = keyTable.length; --i >= 0;) {
		int key = keyTable[i];
		int value = valueTable[i];
		if ((key != 0) || ((key == 0) && (value != 0))) {
			newHashtable.put(key, value);
		}
	}
	this.keyTable = newHashtable.keyTable;
	this.valueTable = newHashtable.valueTable;
	this.threshold = newHashtable.threshold;
}
/**
 * Returns the number of elements contained in the hashtable.
 *
 * @return <CODE>int</CODE> The size of the table
 */
public int size() {
	return elementSize;
}
/**
 * Converts to a rather lengthy String.
 *
 * @return String the ascii representation of the receiver
 */
public String toString() {
	int max = size();
	StringBuffer buf = new StringBuffer();
	buf.append("{"); //$NON-NLS-1$
	for (int i = 0; i < max; ++i) {
		if ((keyTable[i] != 0) || ((keyTable[i] == 0) && (valueTable[i] != 0))) {
			buf.append(keyTable[i]).append("->").append(valueTable[i]); //$NON-NLS-1$
		}
		if (i < max) {
			buf.append(", "); //$NON-NLS-1$
		}
	}
	buf.append("}"); //$NON-NLS-1$
	return buf.toString();
}
}
