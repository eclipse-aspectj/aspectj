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
	private String reverseName;
	private Kind kind;
	
	public Relationship(String name, String reverseName, Kind kind) {
		this.name = name;
		this.reverseName = reverseName;
		this.kind = kind;
	}	
	
	public String getName() {
		return null;
	}

	public Kind getKind() {
		return null;
	}
	
	public String getReverseName() {
		return reverseName;
	}

	public String toString() {
		return name + '/' + reverseName;
	}	
}
