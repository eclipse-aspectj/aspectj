/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/

package org.aspectj.asm;

import java.io.Serializable;
import java.util.List;

import org.aspectj.asm.IRelationship.Kind;

/**
 * Maps from a program element to a list of relationships between that element
 * and othe program elements.  Each element in the list or relationships is
 * uniquely identified by a kind and a relationship name.
 * 
 * @author Mik Kersten
 */
public interface IRelationshipMapper extends Serializable {
 
 	/**
 	 * @return	an empty list if the element is not found.
 	 */
	public List get(IProgramElement source);

	/**
	 * Return a relationship matching the kind and name for the given element.
	 * 
	 * @return	null if the relationship is not found.
	 */
	public IRelationship get(IProgramElement source, IRelationship.Kind kind, String relationshipName);
	
	
	public List/*IRelationship*/ get(String handle);
	
	public void put(IProgramElement source, IRelationship relationship);

	
 
}
