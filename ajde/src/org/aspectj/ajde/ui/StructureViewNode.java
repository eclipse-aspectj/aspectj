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

 
package org.aspectj.ajde.ui;

import java.util.List;

import org.aspectj.asm.StructureNode;

/**
 * @author Mik Kersten
 */ 
public interface StructureViewNode {
	
	public StructureNode getStructureNode();
	
	public AbstractIcon getIcon();
	
	/**
	 * Add a child node.
	 */
	public void add(StructureViewNode child);
	
	/**
	 * Remove a child node.
	 */
	public void remove(StructureViewNode child);
	
	/**
	 * @return	an empty list if there are no children
	 */
	public List getChildren();
}
