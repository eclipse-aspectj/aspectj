/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This holds on to all CrosscuttingMembers for a world.  It handles 
 * management of change.
 * 
 * @author Jim Hugunin
 */
public class CrosscuttingMembersSet {
	private World world;
	private Map members = new HashMap();
	
	private List shadowMungers = null;
	private List typeMungers = null;
	private List declareSofts = null;
	private List declareParents = null;
	private List declareDominates = null;
	
	public CrosscuttingMembersSet(World world) {
		this.world = world;
	}

	/**
	 * @return whether or not that was a change to the global signature
	 * 			XXX for efficiency we will need a richer representation than this
	 */
	public boolean addOrReplaceAspect(ResolvedTypeX aspectType) {
		CrosscuttingMembers xcut = (CrosscuttingMembers)members.get(aspectType);
		if (xcut == null) {
			members.put(aspectType, aspectType.collectCrosscuttingMembers());
			clearCaches();
			return true;
		} else {
			if (xcut.replaceWith(aspectType.collectCrosscuttingMembers())) {
				clearCaches();
				return true;
			} else {
				return false;
			}
		}
	}
	
	public boolean deleteAspect(TypeX aspectType) {
		boolean isAspect = members.remove(aspectType) != null;
		clearCaches();
		return isAspect;
	}
	
	//XXX only for testing
	public void addFixedCrosscuttingMembers(ResolvedTypeX aspectType) {
		members.put(aspectType, aspectType.crosscuttingMembers);
		clearCaches();
	}
	
	
	private void clearCaches() {
		shadowMungers = null;
		typeMungers = null;
		declareSofts = null;
		declareParents = null;
		declareDominates = null;
	}
	
	
	public List getShadowMungers() {
		if (shadowMungers == null) {
			ArrayList ret = new ArrayList();
			for (Iterator i = members.values().iterator(); i.hasNext(); ) {
				ret.addAll(((CrosscuttingMembers)i.next()).getShadowMungers());
			}
			shadowMungers = ret;
		}
		return shadowMungers;
	}
	
	public List getTypeMungers() {
		if (typeMungers == null) {
			ArrayList ret = new ArrayList();
			for (Iterator i = members.values().iterator(); i.hasNext(); ) {
				ret.addAll(((CrosscuttingMembers)i.next()).getTypeMungers());
			}
			typeMungers = ret;
		}
		return typeMungers;
	}
	
	public List getDeclareSofts() {
		if (declareSofts == null) {
			ArrayList ret = new ArrayList();
			for (Iterator i = members.values().iterator(); i.hasNext(); ) {
				ret.addAll(((CrosscuttingMembers)i.next()).getDeclareSofts());
			}
			declareSofts = ret;
		}
		return declareSofts;
	}
	
	public List getDeclareParents() {
		if (declareParents == null) {
			ArrayList ret = new ArrayList();
			for (Iterator i = members.values().iterator(); i.hasNext(); ) {
				ret.addAll(((CrosscuttingMembers)i.next()).getDeclareParents());
			}
			declareParents = ret;
		}
		return declareParents;
	}
	
	public List getDeclareDominates() {
		if (declareDominates == null) {
			ArrayList ret = new ArrayList();
			for (Iterator i = members.values().iterator(); i.hasNext(); ) {
				ret.addAll(((CrosscuttingMembers)i.next()).getDeclareDominates());
			}
			declareDominates = ret;
		}
		return declareDominates;
	}
	
	
	
}
