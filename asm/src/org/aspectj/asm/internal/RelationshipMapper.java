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

package org.aspectj.asm.internal;

import java.util.*;

import org.aspectj.asm.*;

/**
 * @author Mik Kersten
 */
public class RelationshipMapper extends HashMap implements IRelationshipMapper {
	
	public List get(IProgramElement source) {
		List relationships = (List)super.get(source);
		if (relationships == null) {
			return Collections.EMPTY_LIST;
		} else {
			return relationships;
		}
	}

	/**
	 * @return	null if the relationship is not found.
	 */
	public IRelationship get(IProgramElement source, IRelationship.Kind kind, String relationshipName) {
		List relationships = get(source);
		for (Iterator it = relationships.iterator(); it.hasNext(); ) {
			IRelationship curr = (IRelationship)it.next();
			if (curr.getKind() == kind && curr.getName() == relationshipName) {
				return curr;
			}
		}
		return null;
	}

	public List get(String handle) {
		throw new RuntimeException("unimplemented");
	}

	public void put(IProgramElement source, IRelationship relationship) {
		List list = (List)super.get(source);
		if (list == null) {
			list = new ArrayList();
			list.add(relationship);
			super.put(source, list);
		} else {
			boolean matched = false;
			for (Iterator it = list.iterator(); it.hasNext(); ) {
				IRelationship curr = (IRelationship)it.next();
				if (curr.getName().equals(relationship.getName())) {
					curr.getTargets().addAll(relationship.getTargets());
					matched = true;
				}
			}
			if (!matched) list.add(relationship);
		}
	}
	
	// TODO: add a remove, and a clear all

	private static class RelationshipTable {
		private IRelationship relationship;
		private Map map;
		
		public RelationshipTable(IRelationship relationship) {
			this.relationship = relationship;
			map = new HashMap();	
		}
		
		public Map getMap() {
			return map;
		}

		public IRelationship getRelationship() {
			return relationship;
		}
	}

}
