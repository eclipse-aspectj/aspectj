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

import java.util.List;

/**
 * Puts two lists of GenericTreeNode children
 * in traversal order for comparison purposes.
 * Given two lists, produce two lists which
 * should be the comparison order of the two.
 * In the case of set comparison, this will 
 * impose an ordering on the produced lists 
 * such that the equal elements are pairs.
 * In the case of list comparison, it may
 * impose an ordering if the comparator itself
 * does not. 
 * All Lists must contain only GenericTreeNode.
 */
public interface GenericTreeNodeListOrdererI {
    /**
     * Produce lists representing the proper transformation of 
     * the children for a given visitor.  
     * To use the existing lists, return them in a new array.
     * You may return null for one or both Lists. 
     * @param lhs the List representing the left-hand-side
     *            which contains only GenericTreeNode
     * @param rhs the List representing the right-hand-side
     *            which contains only GenericTreeNode
     * @return two lists List[] (0 => lhs, 1 => rhs)
     */
    public List[] produceLists(List lhs, List rhs);
}
