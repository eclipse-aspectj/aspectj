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

import org.eclipse.jdt.internal.compiler.util.CharOperation;

public class CharArrayCache {
	// to avoid using Enumerations, walk the individual tables skipping nulls
	public char[] keyTable[];
	public int valueTable[];
	int elementSize; // number of elements in the table
	int threshold;
/**
 * Constructs a new, empty hashtable. A default capacity is used.
 * Note that the hashtable will automatically grow when it gets full.
 */
public CharArrayCache() {
	this(13);
}
/**
 * Constructs a new, empty hashtable with the specified initial
 * capacity.
 * @param initialCapacity int
 *	the initial number of buckets
 */
public CharArrayCache(int initialCapacity) {
	this.elementSize = 0;
	this.threshold = (int) (initialCapacity * 0.66f);
	this.keyTable = new char[initialCapacity][];
	this.valueTable = new int[initialCapacity];
}
/**
 * Clears the hash table so that it has no more elements in it.
 */
public void clear() {
	for (int i = keyTable.length; --i >= 0;) {
		keyTable[i] = null;
		valueTable[i] = 0;
	}
	elementSize = 0;
}
/** Returns true if the collection contains an element for the key.
 *
 * @param char[] key the key that we are looking for
 * @return boolean
 */
public boolean containsKey(char[] key) {
	int index = hashCodeChar(key);
	while (keyTable[index] != null) {
		if (CharOperation.equals(keyTable[index], key))
			return true;
		index = (index + 1) % keyTable.length;
	}
	return false;
}
/** Gets the object associated with the specified key in the
 * hashtable.
 * @param key <CODE>char[]</CODE> the specified key
 * @return int the element for the key or -1 if the key is not
 *	defined in the hash table.
 */
public int get(char[] key) {
	int index = hashCodeChar(key);
	while (keyTable[index] != null) {
		if (CharOperation.equals(keyTable[index], key))
			return valueTable[index];
		index = (index + 1) % keyTable.length;
	}
	return -1;
}
private int hashCodeChar(char[] val) {
	int length = val.length;
	int hash = 0;
	int n = 2; // number of characters skipped
	for (int i = 0; i < length; i += n) {
		hash += val[i];
	}
	return (hash & 0x7FFFFFFF) % keyTable.length;
}
/**
 * Puts the specified element into the hashtable, using the specified
 * key.  The element may be retrieved by doing a get() with the same key.
 * The key and the element cannot be null. 
 * 
 * @param key <CODE>Object</CODE> the specified key in the hashtable
 * @param value <CODE>int</CODE> the specified element
 * @return int the old value of the key, or -1 if it did not have one.
 */
public int put(char[] key, int value) { 
	int index = hashCodeChar(key);
	while (keyTable[index] != null) {
		if (CharOperation.equals(keyTable[index], key))
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
/**
 * Rehashes the content of the table into a bigger table.
 * This method is called automatically when the hashtable's
 * size exceeds the threshold.
 */
private void rehash() {
	CharArrayCache newHashtable = new CharArrayCache(keyTable.length * 2);
	for (int i = keyTable.length; --i >= 0;)
		if (keyTable[i] != null)
			newHashtable.put(keyTable[i], valueTable[i]);

	this.keyTable = newHashtable.keyTable;
	this.valueTable = newHashtable.valueTable;
	this.threshold = newHashtable.threshold;
}
/** Remove the object associated with the specified key in the
 * hashtable.
 * @param key <CODE>char[]</CODE> the specified key
 */
public void remove(char[] key) {
	int index = hashCodeChar(key);
	while (keyTable[index] != null) {
		if (CharOperation.equals(keyTable[index], key)) {
			valueTable[index] = 0;
			keyTable[index] = null;
			return;
		}
		index = (index + 1) % keyTable.length;
	}
}
/**
 * Returns the key corresponding to the value. Returns null if the
 * receiver doesn't contain the value.
 * @param value int the value that we are looking for
 * @return Object
 */
public char[] returnKeyFor(int value) {
	for (int i = keyTable.length; i-- > 0;) {
		if (valueTable[i] == value) {
			return keyTable[i];
		}
	}
	return null;
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
 * return String the ascii representation of the receiver
 */
public String toString() {
	int max = size();
	StringBuffer buf = new StringBuffer();
	buf.append("{"); //$NON-NLS-1$
	for (int i = 0; i < max; ++i) {
		if (keyTable[i] != null) {
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
