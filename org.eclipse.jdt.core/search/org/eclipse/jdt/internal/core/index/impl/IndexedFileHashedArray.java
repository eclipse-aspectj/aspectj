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
package org.eclipse.jdt.internal.core.index.impl;

import java.util.ArrayList;

import org.eclipse.jdt.internal.core.index.IDocument;

public final class IndexedFileHashedArray {

private IndexedFile elements[];
private int elementSize; // number of elements in the table
private int threshold;
private int lastId;
private ArrayList replacedElements;

public IndexedFileHashedArray(int size) {
	if (size < 7) size = 7;
	this.elements = new IndexedFile[2 * size + 1];
	this.elementSize = 0;
	this.threshold = size + 1; // size is the expected number of elements
	this.lastId = 0;
	this.replacedElements = null;
}

public IndexedFile add(IDocument document) {
	return add(new IndexedFile(document, ++lastId));
}

private IndexedFile add(IndexedFile file) {
	int length = elements.length;
	String path = file.getPath();
	int index = (path.hashCode() & 0x7FFFFFFF) % length;
	IndexedFile current;
	while ((current = elements[index]) != null) {
		if (current.getPath().equals(path)) {
			if (replacedElements == null) replacedElements = new ArrayList(5);
			replacedElements.add(current);
			return elements[index] = file;
		}
		if (++index == length) index = 0;
	}
	elements[index] = file;

	// assumes the threshold is never equal to the size of the table
	if (++elementSize > threshold) grow();
	return file;
}

public IndexedFile[] asArray() {
	IndexedFile[] array = new IndexedFile[lastId];
	for (int i = 0, length = elements.length; i < length; i++) {
		IndexedFile current = elements[i];
		if (current != null)
			array[current.fileNumber - 1] = current;
	}
	if (replacedElements != null) {
		for (int i = 0, length = replacedElements.size(); i < length; i++) {
			IndexedFile current = (IndexedFile) replacedElements.get(i);
			array[current.fileNumber - 1] = current;
		}
	}
	return array;
}

public IndexedFile get(String path) {
	int length = elements.length;
	int index = (path.hashCode() & 0x7FFFFFFF) % length;
	IndexedFile current;
	while ((current = elements[index]) != null) {
		if (current.getPath().equals(path)) return current;
		if (++index == length) index = 0;
	}
	return null;
}

private void grow() {
	IndexedFileHashedArray newArray = new IndexedFileHashedArray(elementSize * 2); // double the number of expected elements
	for (int i = 0, length = elements.length; i < length; i++)
		if (elements[i] != null)
			newArray.add(elements[i]);

	// leave replacedElements as is
	this.elements = newArray.elements;
	this.elementSize = newArray.elementSize;
	this.threshold = newArray.threshold;
}

public int size() {
	return elementSize + (replacedElements == null ? 0 : replacedElements.size());
}

public String toString() {
	String s = ""; //$NON-NLS-1$
	IndexedFile[] files = asArray();
	for (int i = 0, length = files.length; i < length; i++)
		s += files[i].toString() + "\n"; 	//$NON-NLS-1$
	return s;
}
}