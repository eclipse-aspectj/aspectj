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

package org.aspectj.testing.compare;
/**
 * Visit a node-pair in the trees, 
 * signalling whether to continue traversal.
 */
public interface GenericTreeNodesVisitorI {
    /**
     * Visit nodes in parallel trees.
     * One (but not both) of the input nodes may be null.
     * @param lhs the GenericTreeNode on the left-hand-side of a binary operation
     *            (may be null)
     * @param lhs the GenericTreeNode on the right-hand-side of a binary operation
     *            (may be null)
     * @return true if should continue visiting 
     */
    public boolean visit(GenericTreeNode lhs, GenericTreeNode rhs);
}

