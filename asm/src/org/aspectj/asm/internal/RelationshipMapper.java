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
	
	public IRelationship get(IProgramElement source) {
		return (IRelationship)super.get(source);
	}

	public IRelationship get(String handle) {
		throw new RuntimeException("unimplemented");
	}

	public void put(IProgramElement source, IRelationship relationship) {
		super.put(source, relationship);
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
