/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Andy Clement			Initial implementation
 * ******************************************************************/

package org.aspectj.weaver;

/**
 * Tag interface - methods and types can be declaring elements for type variables. See the TypeVariable class which holds onto the
 * declaring element
 */
public interface TypeVariableDeclaringElement {
	TypeVariable getTypeVariableNamed(String name);
}
