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
package org.eclipse.jdt.internal.core.builder;

import java.util.*;

public class WorkQueue {

ArrayList needsCompileList;
ArrayList compiledList;

public WorkQueue() {
	this.needsCompileList = new ArrayList(11);
	this.compiledList = new ArrayList(11);
}

public void add(String element) {
	needsCompileList.add(element);
}

public void addAll(String[] elements) {
	for (int i = 0, length = elements.length; i < length; i++)
		add(elements[i]);
}

public void clear() {
	this.needsCompileList.clear();
	this.compiledList.clear();
}	

public void finished(String element) {
	needsCompileList.remove(element);
	compiledList.add(element);
}

public boolean isCompiled(String element) {
	return compiledList.contains(element);
}

public boolean isWaiting(String element) {
	return needsCompileList.contains(element);
}

public String toString() {
	return "WorkQueue: " + needsCompileList; //$NON-NLS-1$
}
}