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

import java.util.List;

import org.aspectj.asm.*;
import org.aspectj.asm.IRelationship;
import org.aspectj.asm.IRelationship.Kind;


/**
 * @author Mik Kersten
 */
public class Relationship implements IRelationship {
	
	private String name;
	private Kind kind;
	private String sourceHandle;
	private List targets;
	
	public Relationship(
		String name, 
		Kind kind,
		String sourceHandle,
		List targets) {
			
		this.name = name;
		this.kind = kind;
		this.sourceHandle = sourceHandle;
		this.targets = targets;
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

}
