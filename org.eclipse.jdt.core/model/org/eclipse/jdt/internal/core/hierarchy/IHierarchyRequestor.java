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
package org.eclipse.jdt.internal.core.hierarchy;

import org.eclipse.jdt.internal.compiler.env.IGenericType;

public interface IHierarchyRequestor {
/**
 * Connect the supplied type to its superclass & superinterfaces.
 * The superclass & superinterfaces are the identical binary or source types as
 * supplied by the name environment.
 */

public void connect(IGenericType suppliedType, IGenericType superclass, IGenericType[] superinterfaces);
}
