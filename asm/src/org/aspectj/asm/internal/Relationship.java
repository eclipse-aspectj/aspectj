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

}
