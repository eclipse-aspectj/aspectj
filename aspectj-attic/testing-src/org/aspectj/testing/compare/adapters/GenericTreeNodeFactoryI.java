/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.testing.compare.adapters;

import org.aspectj.testing.compare.GenericTreeNode;
/**
 * Encapsulate the implementation of an factory to create
 * a GenericTreeNode tree from a given input type, to permit
 * a general, pluggable factory to operate based on source type.
 */
public interface GenericTreeNodeFactoryI {
    /** @return the expected Class of the root node supported by this factory */
    public Class getRootClass();

    /**
     * Create a wrapped generic tree with the input root tree as delegates.
     * @param root the {rootClass} root of the tree to convert - never null
     * @throws IllegalArgumentException if root is null or not assignable to rootClass
     */
    public GenericTreeNode createGenericTreeNode(Object root, GenericTreeNode parent);
}

