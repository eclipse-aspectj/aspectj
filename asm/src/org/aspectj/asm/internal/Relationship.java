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
 *     Andy Clement    Extensions for better IDE representation
 * ******************************************************************/


package org.aspectj.asm.internal;

import java.util.List;

import org.aspectj.asm.IRelationship;
//import org.aspectj.asm.IRelationship.Kind;


/**
 * @author Mik Kersten
 */
public class Relationship implements IRelationship {
	
	private static final long serialVersionUID = 3855166397957609120L;

	private String name;
	private Kind kind;
	private String sourceHandle;
	private List targets;
	private boolean hasRuntimeTest;
	
	public Relationship(
		String name, 
		Kind kind,
		String sourceHandle,
		List targets,
		boolean runtimeTest) {
			
		this.name = name;
		this.kind = kind;
		this.sourceHandle = sourceHandle;
		this.targets = targets;
		this.hasRuntimeTest = runtimeTest;
	}	
	
	public String getName() {
		return name;
	}

	public Kind getKind() {
		return kind;
	}

	public String toString() {
		return name;
	}	
	
	public String getSourceHandle() {
		return sourceHandle;
	}

	public List getTargets() {
		return targets;
	}
	
	public boolean addTarget(String handle) {
		if (targets.contains(handle)) return false;
		targets.add(handle);
		return true;
	}
	
	public boolean hasRuntimeTest() {
		return hasRuntimeTest;
	}
	
	// For repairing the relationship map on incremental builds, we need
	// to know the direction of the relationship: either 'affects' or 'affected by'
	// this set are considered the 'affects' relationship.  If we know which direction
	// it is in, we know which ones should be removed when a particular resource
	// is modified because the subsequent reweave will re-add it.
	public boolean isAffects() {
	    // TODO should be a well defined set (enum type) with a flag for this...
		return name.equals("advises") || 
		        name.equals("declares on") || 
		        name.equals("softens") ||
		        name.equals("matched by") || 
		        name.equals("declared on") || 
		        name.equals("annotates");
	}

}
