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
package org.eclipse.jdt.internal.core.search;

import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.search.IJavaSearchScope;

public abstract class AbstractSearchScope implements IJavaSearchScope {

/**
 * @see IJavaSearchScope#includesBinaries()
 * @deprecated
 */
public boolean includesBinaries() {
	return true;
}

/**
 * @see IJavaSearchScope#includesClasspaths()
 * @deprecated
 */
public boolean includesClasspaths() {
	return true;
}

/* (non-Javadoc)
 * Process the given delta and refresh its internal state if needed.
 * Returns whether the internal state was refreshed.
 */
public abstract void processDelta(IJavaElementDelta delta);

/**
 * @see IJavaSearchScope#setIncludesBinaries(boolean)
 * @deprecated
 */
public void setIncludesBinaries(boolean includesBinaries) {
}

/**
 * @see IJavaSearchScope#setIncludesClasspaths(boolean)
 * @deprecated
 */
public void setIncludesClasspaths(boolean includesClasspaths) {
}

}
