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
	
	private Map/*IRelationship*/ relationships = new HashMap();
	
	public RelationshipMapper(List availableRelationships) {
		for (Iterator it = availableRelationships.iterator(); it.hasNext(); ) {
			IRelationship r = (IRelationship)it.next();
			relationships.put(r, new HashMap());
		}
	}
	
	public List getRelationshipsForElement(IProgramElement source, IRelationship relationship) {
		Map map = (Map)relationships.get(relationship);
		return (List)map.get(source);
	}

	/**
	 * Creates the relationship if not present.
	 */
	public void putRelationshipForElement(IProgramElement source, IRelationship relationship, List targets) {
		Map map = (Map)relationships.get(relationship);
		if (map == null) {
			map = new HashMap();
			relationships.put(relationship, map);
		}
		map.put(source, targets);
	}

	public void putRelationshipForElement(IProgramElement source, IRelationship relationship, IProgramElement target) {
		Map map = (Map)relationships.get(relationship);
		if (map == null) {
			map = new HashMap();
			relationships.put(relationship, map);
		}
		List targetList = (List)map.get(source);	
		if (targetList == null) {
			targetList = new ArrayList();
			map.put(source, targetList);
		}
		targetList.add(target);
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
//	public List getRelationshipsForElement(
//		IProgramElement source,
//		IRelationship relationship) {
//			
//		String signatureKey = (List)getRelationshipsForElement(source.getSignatureKey(), relationship);
//		
//	}
//
//	public void putRelationshipForElement(
//		IProgramElement source,
//		IRelationship kind,
//		IProgramElement target) {
//
//	}
//
//	public void putRelationshipForElement(
//		IProgramElement source,
//		IRelationship relationship,
//		List targets) {
//
//	}
	
	

}
