/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mik Kersten     initial implementation
 * ******************************************************************/

package org.aspectj.asm;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Maps from a program element handles to a list of relationships between that element and other program elements. Each element in
 * the list or relationships is uniquely identified by a kind and a relationship name. For example, the advice affecting a
 * particular shadow (e.g. method call) can be retrieved by calling <CODE>get</CODE> on the handle for that method. Symmetrically
 * the method call shadows that an advice affects can be retrieved.
 *
 * <p>
 * The elements can be stored and looked up as IProgramElement(s), in which cases the element corresponding to the handle is looked
 * up in the containment hierarchy.
 * </p>
 *
 * <p>
 * put/get methods taking IProgramElement as a parameter are for convenience only. They work identically to calling their
 * counterparts with IProgramElement.getIdentifierHandle()
 * </p>
 *
 * @author Mik Kersten
 * @author Andy Clement
 */
public interface IRelationshipMap extends Serializable {

	/**
	 * @return list of relationships or null if the source element has no relationships
	 */
	List<IRelationship> get(IProgramElement sourceProgramElement);

	/**
	 * @return list of relationships or null if the source element has no relationships
	 */
	List<IRelationship> get(String sourceHandle);

	/**
	 * Return a relationship matching the kind and name for the given element.
	 *
	 * @return null if the relationship is not found.
	 */
	IRelationship get(IProgramElement source, IRelationship.Kind kind, String relationshipName, boolean runtimeTest,
					  boolean createIfMissing);

	/**
	 * Return a relationship matching the kind and name for the given element.
	 *
	 * @return null if the relationship is not found.
	 */
	IRelationship get(IProgramElement source, IRelationship.Kind kind, String relationshipName);

	/**
	 * Return a relationship matching the kind and name for the given element. Creates the relationship if not found.
	 *
	 * @return null if the relationship is not found.
	 */
	IRelationship get(String source, IRelationship.Kind kind, String relationshipName, boolean runtimeTest,
					  boolean createIfMissing);

	void put(IProgramElement source, IRelationship relationship);

	void put(String handle, IRelationship relationship);

	boolean remove(String handle, IRelationship relationship);

	void removeAll(String source);

	/**
	 * Clear all of the relationships in the map.
	 */
	void clear();

	Set<String> getEntries();

}
