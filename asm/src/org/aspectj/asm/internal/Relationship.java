/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
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
	private IProgramElement source;
	private List targets;
	
	public Relationship(
		String name, 
		Kind kind,
		IProgramElement source,
		List targets) {
			
		this.name = name;
		this.kind = kind;
		this.source = source;
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
	
	public IProgramElement getSource() {
		return source;
	}

	public List getTargets() {
		return targets;
	}

}
