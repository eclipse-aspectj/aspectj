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

//import org.aspectj.asm.IRelationship.Kind;

/**
 * Maps from a program element handles to a list of relationships between that element
 * and othe program elements.  Each element in the list or relationships is
 * uniquely identified by a kind and a relationship name.
 * 
 * The elemetns can be stored and looked up as IProgramElement(s), in which cases the 
 * element corresponding to the handle is looked up in the containment hierarchy.
 * 
 * put/get methods taking IProgramElement as a parameter are for convenience only.  
 * They work identically to calling their counterparts with IProgramElement.getIdentifierHandle()
 * 
 * @author Mik Kersten
 */
public interface IRelationshipMap extends Serializable {
 
 	/**
 	 * @return	an empty list if the element is not found.
 	 */
	public List/*IRelationship*/ get(IProgramElement source);

	/**
	 * @return	an empty list if the element is not found.
	 */	
	public List/*IRelationship*/ get(String handle);

	/**
	 * Return a relationship matching the kind and name for the given element.
	 * 
	 * @return	null if the relationship is not found.
	 */
	public IRelationship get(IProgramElement source, IRelationship.Kind kind, String relationshipName);

	/**
	 * Return a relationship matching the kind and name for the given element.
	 * Creates the relationship if not found.
	 * 
	 * @return	null if the relationship is not found.
	 */
	public IRelationship get(String source, IRelationship.Kind kind, String relationshipName);
	
	public void put(IProgramElement source, IRelationship relationship);

	public void put(String handle, IRelationship relationship);
	
	public void remove(String handle, IRelationship relationship);
	
	public void removeAll(String source);
	
	/**
	 * Clear all of the relationships in the map.
	 */
	public void clear();
 
}
