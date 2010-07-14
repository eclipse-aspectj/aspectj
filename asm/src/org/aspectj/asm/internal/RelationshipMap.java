/* *******************************************************************
 * Copyright (c) 2003,2010 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 *     Andy Clement
 * ******************************************************************/

package org.aspectj.asm.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.asm.IRelationship.Kind;
import org.aspectj.asm.IRelationshipMap;

/**
 * @author Mik Kersten
 * @author Andy Clement
 */
public class RelationshipMap extends HashMap<String, List<IRelationship>> implements IRelationshipMap {

	private static final long serialVersionUID = 496638323566589643L;

	public RelationshipMap() {
	}

	public List<IRelationship> get(String handle) {
		List<IRelationship> relationships = super.get(handle);
		if (relationships == null) {
			return null;
		} else {
			return relationships;
		}
	}

	public List<IRelationship> get(IProgramElement source) {
		return get(source.getHandleIdentifier());
	}

	public IRelationship get(String source, IRelationship.Kind kind, String relationshipName, boolean runtimeTest,
			boolean createIfMissing) {
		List<IRelationship> relationships = get(source);
		if (relationships == null) {
			if (!createIfMissing) {
				return null;
			}
			relationships = new ArrayList<IRelationship>();
			IRelationship rel = new Relationship(relationshipName, kind, source, new ArrayList<String>(), runtimeTest);
			relationships.add(rel);

			super.put(source, relationships);
			return rel;
		} else {
			for (Iterator<IRelationship> it = relationships.iterator(); it.hasNext();) {
				IRelationship curr = it.next();
				if (curr.getKind() == kind && curr.getName().equals(relationshipName) && curr.hasRuntimeTest() == runtimeTest) {
					return curr;
				}
			}
			if (createIfMissing) {
				// At this point we did find some relationships for 'source' but not one that looks like what we are
				// after (either the kind or the name or the dynamictests setting don't match)
				IRelationship rel = new Relationship(relationshipName, kind, source, new ArrayList<String>(), runtimeTest);
				relationships.add(rel);
				return rel;
			}
		}
		return null;
	}

	public IRelationship get(IProgramElement source, IRelationship.Kind kind, String relationshipName, boolean runtimeTest,
			boolean createIfMissing) {
		return get(source.getHandleIdentifier(), kind, relationshipName, runtimeTest, createIfMissing);
	}

	public IRelationship get(IProgramElement source, Kind kind, String relationshipName) {
		return get(source, kind, relationshipName, false, true);
	}

	public boolean remove(String source, IRelationship relationship) {
		List<IRelationship> list = super.get(source);
		if (list != null) {
			return list.remove(relationship);
			// boolean matched = false;
			// for (Iterator it = list.iterator(); it.hasNext(); ) {
			// IRelationship curr = (IRelationship)it.next();
			// if (curr.getName().equals(relationship.getName())) {
			// curr.getTargets().addAll(relationship.getTargets());
			// matched = true;
			// }
			// }
			// if (!matched) list.remove(relationship);
		}
		return false;
	}

	public void removeAll(String source) {
		super.remove(source);
	}

	public void put(String source, IRelationship relationship) {
		List<IRelationship> existingRelationships = super.get(source);
		if (existingRelationships == null) {
			// new entry
			existingRelationships = new ArrayList<IRelationship>();
			existingRelationships.add(relationship);
			super.put(source, existingRelationships);
		} else {
			boolean matched = false;
			for (IRelationship existingRelationship : existingRelationships) {
				if (existingRelationship.getName().equals(relationship.getName())
						&& existingRelationship.getKind() == relationship.getKind()) {
					existingRelationship.getTargets().addAll(relationship.getTargets());
					matched = true;
				}
			}
			if (matched) {
				// bug?
				System.err.println("matched = true");
			}
			if (matched) {
				existingRelationships.add(relationship); // Is this a bug, will it give us double entries?
			}
		}
	}

	public void put(IProgramElement source, IRelationship relationship) {
		put(source.getHandleIdentifier(), relationship);
	}

	public void clear() {
		super.clear();
	}

	public Set<String> getEntries() {
		return keySet();
	}

}
