/* *******************************************************************
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
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

	/**
	 * Creates the relationship if not present.
	 */
	public void put(IProgramElement source, IRelationship relationship) {
		super.put(source, relationship);
	}

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
