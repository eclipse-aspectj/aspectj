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
 * TODO: add a remove, and a clear all
 * 
 * @author Mik Kersten
 * 
 */
public class RelationshipMap extends HashMap implements IRelationshipMap {
	
	private IHierarchy hierarchy;
	
	public RelationshipMap(IHierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}

	public List get(String handle) {
		List relationships = (List)super.get(handle);
		if (relationships == null) {
			return null;
		} else {
			return relationships;
		}
	}
	
	public List get(IProgramElement source) {
		return get(source.getHandleIdentifier());
	}

	public IRelationship get(String source, IRelationship.Kind kind, String relationshipName) {
		List relationships = get(source);
		if (relationships == null) {
			relationships = new ArrayList();
			IRelationship rel = new Relationship(relationshipName, kind, source, new ArrayList());
			relationships.add(rel);
			super.put(source, relationships);
			return rel;
		} else {
			for (Iterator it = relationships.iterator(); it.hasNext(); ) {
				IRelationship curr = (IRelationship)it.next();
				if (curr.getKind() == kind && curr.getName().equals(relationshipName)) {
					return curr;
				}
			}
		}
		return null;
	}

	public IRelationship get(IProgramElement source, IRelationship.Kind kind, String relationshipName) {
		return get(source.getHandleIdentifier(), kind, relationshipName);
	}
	
	public void remove(String source, IRelationship relationship) {
		List list = (List)super.get(source);
		if (list != null) {
			boolean matched = false;
			for (Iterator it = list.iterator(); it.hasNext(); ) {
				IRelationship curr = (IRelationship)it.next();
				if (curr.getName().equals(relationship.getName())) {
					curr.getTargets().addAll(relationship.getTargets());
					matched = true;
				}
			}
			if (!matched) list.remove(relationship);
		}		
	}

	public void removeAll(String source) {
		List list = (List)super.remove(source);	
	}
	
	public void put(String source, IRelationship relationship) {
		System.err.println(">> for: " + source + ", put::" + relationship);
		
		List list = (List)super.get(source);
		if (list == null) {
			list = new ArrayList();
			list.add(relationship);
			super.put(source, list);
		} else {
			boolean matched = false;
			for (Iterator it = list.iterator(); it.hasNext(); ) {
				IRelationship curr = (IRelationship)it.next();
				if (curr.getName().equals(relationship.getName())
					&& curr.getKind() == relationship.getKind()) {
					curr.getTargets().addAll(relationship.getTargets());
					matched = true;
				}
			}
			if (matched) list.add(relationship);
		}
	}

	public void put(IProgramElement source, IRelationship relationship) {
		put(source.getHandleIdentifier(), relationship);
	}

	public void clear() {
		super.clear();
	}

}
