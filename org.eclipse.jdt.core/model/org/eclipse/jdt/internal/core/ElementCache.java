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

import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.util.LRUCache;

/**
 * An LRU cache of <code>JavaElements</code>.
 */
public class ElementCache extends OverflowingLRUCache {
/**
 * Constructs a new element cache of the given size.
 */
public ElementCache(int size) {
	super(size);
}
/**
 * Constructs a new element cache of the given size.
 */
public ElementCache(int size, int overflow) {
	super(size, overflow);
}
/**
 * Returns true if the element is successfully closed and
 * removed from the cache, otherwise false.
 *
 * <p>NOTE: this triggers an external removal of this element
 * by closing the element.
 */
protected boolean close(LRUCacheEntry entry) {
	IOpenable element = (IOpenable) entry._fKey;
	try {
		if (element.hasUnsavedChanges()) {
			return false;
		} else {
			// We must close an entire JarPackageFragmentRoot at once.
			if (element instanceof JarPackageFragment) {
				JarPackageFragment packageFragment= (JarPackageFragment) element;
				JarPackageFragmentRoot root = (JarPackageFragmentRoot) packageFragment.getParent();
				root.close();
			} else {
				element.close();
			}
			return true;
		}
	} catch (JavaModelException npe) {
		return false;
	}
}
	/**
	 * Returns a new instance of the reciever.
	 */
	protected LRUCache newInstance(int size, int overflow) {
		return new ElementCache(size, overflow);
	}
}
