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
 * Factory to produce orderers per children list pair.
 * Using a factory permits generation/selection of the right orderer 
 * for each set of children to be compared, potentially considering
 * the visitor expectations.  Note that while orderers may change during
 * traversals, visitors do not.
 */
public interface GenericTreeNodeListOrdererFactoryI {
    /** 
     * Produce the correct orderer for the children of the given GenericTreeNodes
     * @return GenericTreeNodeListOrdererI for children, or null if none to be used 
     */
    public GenericTreeNodeListOrdererI produce(GenericTreeNode lhs, GenericTreeNode rhs,
                                               GenericTreeNodesVisitorI visitor);
}
