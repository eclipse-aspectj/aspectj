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

package org.aspectj.asm.internal;

import java.util.*;

import org.aspectj.asm.*;
import org.aspectj.asm.IRelationship.Kind;

/**
 * TODO: add a remove, and a clear all
 * 
 * @author Mik Kersten
 * 
 */
public class RelationshipMap extends HashMap implements IRelationshipMap {
	
	private static final long serialVersionUID = 496638323566589643L;

//	// As this gets serialized, make the hierarchy transient and
//	// settable
//	private transient IHierarchy hierarchy;
	
	public RelationshipMap() { }
	
	public RelationshipMap(IHierarchy hierarchy) {
//		this.hierarchy = hierarchy;
	}

	public void setHierarchy(IHierarchy hierarchy) {
		// commented out as field never read !
//		this.hierarchy = hierarchy;
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

	public IRelationship get(String source, IRelationship.Kind kind, 
	                         String relationshipName,boolean runtimeTest,boolean createIfMissing) {
		List relationships = get(source);
		if (relationships == null) {
			if (!createIfMissing) return null;
			relationships = new ArrayList();
			IRelationship rel = new Relationship(relationshipName, kind, source, new ArrayList(),runtimeTest);
			relationships.add(rel);
			super.put(source, relationships);
			return rel;
		} else {
			for (Iterator it = relationships.iterator(); it.hasNext(); ) {
				IRelationship curr = (IRelationship)it.next();
				if (curr.getKind() == kind && 
				    curr.getName().equals(relationshipName) &&
				    curr.hasRuntimeTest() == runtimeTest) {
					return curr;
				}
			}
			if (createIfMissing) {
				// At this point we did find some relationships for 'source' but not one that looks like what we are
				// after (either the kind or the name or the dynamictests setting don't match)
				IRelationship rel = new Relationship(relationshipName, kind, source, new ArrayList(),runtimeTest);
				relationships.add(rel);
				return rel;
			}
		}
		return null;
	}

	public IRelationship get(IProgramElement source, IRelationship.Kind kind, String relationshipName, boolean runtimeTest,boolean createIfMissing) {
		return get(source.getHandleIdentifier(), kind, relationshipName,runtimeTest,createIfMissing);
	}
	
	public IRelationship get(IProgramElement source, Kind kind, String relationshipName) {
		return get(source,kind,relationshipName,false,true);
	}
	
	public boolean remove(String source, IRelationship relationship) {
		List list = (List)super.get(source);
		if (list != null) {
			return list.remove(relationship);
//			boolean matched = false;
//			for (Iterator it = list.iterator(); it.hasNext(); ) {
//				IRelationship curr = (IRelationship)it.next();
//				if (curr.getName().equals(relationship.getName())) {
//					curr.getTargets().addAll(relationship.getTargets());
//					matched = true;
//				}
//			}
//			if (!matched) list.remove(relationship);
		}		
		return false;
	}

	public void removeAll(String source) {
		super.remove(source);	
	}
	
	public Object put(Object o, Object p) {
		return super.put(o,p);
	}
	public void put(String source, IRelationship relationship) {
	
		//System.err.println(">> for: " + source + ", put::" + relationship);
		
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
			if (matched) {
				// bug?
				System.err.println("matched = true");
			}
			if (matched) list.add(relationship); // Is this a bug, will it give us double entries?
		}
	}

	public void put(IProgramElement source, IRelationship relationship) {
		put(source.getHandleIdentifier(), relationship);
	}

	public void clear() {
		super.clear();
	}

	public Set getEntries() {
		return keySet();
	}


}
