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
package org.eclipse.jdt.internal.core;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;

/**
 * The cache of java elements to their respective info.
 */
public class JavaModelCache {
	public static final int PKG_CACHE_SIZE = 500;
	public static final int OPENABLE_CACHE_SIZE = 2000;
	
	/**
	 * Active Java Model Info
	 */
	protected JavaModelInfo modelInfo;
	
	/**
	 * Cache of open projects and package fragment roots.
	 */
	protected Map projectAndRootCache;
	
	/**
	 * Cache of open package fragments
	 */
	protected Map pkgCache;

	/**
	 * Cache of open compilation unit and class files
	 */
	protected OverflowingLRUCache openableCache;

	/**
	 * Cache of open children of openable Java Model Java elements
	 */
	protected Map childrenCache;
	
public JavaModelCache() {
	this.projectAndRootCache = new HashMap(50);
	this.pkgCache = new HashMap(PKG_CACHE_SIZE);
	this.openableCache = new ElementCache(OPENABLE_CACHE_SIZE);
	this.childrenCache = new HashMap(OPENABLE_CACHE_SIZE*20); // average 20 chilren per openable
}

public double openableFillingRatio() {
	return this.openableCache.fillingRatio();
}
public int pkgSize() {
	return this.pkgCache.size();
}
	
/**
 *  Returns the info for the element.
 */
public Object getInfo(IJavaElement element) {
	switch (element.getElementType()) {
		case IJavaElement.JAVA_MODEL:
			return this.modelInfo;
		case IJavaElement.JAVA_PROJECT:
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			return this.projectAndRootCache.get(element);
		case IJavaElement.PACKAGE_FRAGMENT:
			return this.pkgCache.get(element);
		case IJavaElement.COMPILATION_UNIT:
		case IJavaElement.CLASS_FILE:
			return this.openableCache.get(element);
		default:
			return this.childrenCache.get(element);
	}
}

/**
 *  Returns the info for this element without
 *  disturbing the cache ordering.
 */
protected Object peekAtInfo(IJavaElement element) {
	switch (element.getElementType()) {
		case IJavaElement.JAVA_MODEL:
			return this.modelInfo;
		case IJavaElement.JAVA_PROJECT:
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			return this.projectAndRootCache.get(element);
		case IJavaElement.PACKAGE_FRAGMENT:
			return this.pkgCache.get(element);
		case IJavaElement.COMPILATION_UNIT:
		case IJavaElement.CLASS_FILE:
			return this.openableCache.peek(element);
		default:
			return this.childrenCache.get(element);
	}
}

/**
 * Remember the info for the element.
 */
protected void putInfo(IJavaElement element, Object info) {
	switch (element.getElementType()) {
		case IJavaElement.JAVA_MODEL:
			this.modelInfo = (JavaModelInfo) info;
			break;
		case IJavaElement.JAVA_PROJECT:
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			this.projectAndRootCache.put(element, info);
			break;
		case IJavaElement.PACKAGE_FRAGMENT:
			this.pkgCache.put(element, info);
			break;
		case IJavaElement.COMPILATION_UNIT:
		case IJavaElement.CLASS_FILE:
			this.openableCache.put(element, info);
			break;
		default:
			this.childrenCache.put(element, info);
	}
}
/**
 * Removes the info of the element from the cache.
 */
protected void removeInfo(IJavaElement element) {
	switch (element.getElementType()) {
		case IJavaElement.JAVA_MODEL:
			this.modelInfo = null;
			break;
		case IJavaElement.JAVA_PROJECT:
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			this.projectAndRootCache.remove(element);
			break;
		case IJavaElement.PACKAGE_FRAGMENT:
			this.pkgCache.remove(element);
			break;
		case IJavaElement.COMPILATION_UNIT:
		case IJavaElement.CLASS_FILE:
			this.openableCache.remove(element);
			break;
		default:
			this.childrenCache.remove(element);
	}
}
}
