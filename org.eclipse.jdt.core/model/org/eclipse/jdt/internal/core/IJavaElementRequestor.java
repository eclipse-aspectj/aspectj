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

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;

/**
 * This interface is used by IRequestorNameLookup. As results
 * are found by IRequestorNameLookup, they are reported to this
 * interface. An IJavaElementRequestor is able to cancel
 * at any time (i.e. stop receiving results), by responding
 * <code>true</code> to <code>#isCancelled</code>.
 */
public interface IJavaElementRequestor {
public void acceptField(IField field);
public void acceptInitializer(IInitializer initializer);
public void acceptMemberType(IType type);
public void acceptMethod(IMethod method);
public void acceptPackageFragment(IPackageFragment packageFragment);
public void acceptType(IType type);
/**
 * Returns <code>true</code> if this IJavaElementRequestor does
 * not want to receive any more results.
 */
boolean isCanceled();
}
