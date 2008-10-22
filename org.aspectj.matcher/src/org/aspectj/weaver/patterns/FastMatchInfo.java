/* *******************************************************************
 * Copyright (c) 2004 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Jim Hugunin     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.patterns;

import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.Shadow.Kind;


public class FastMatchInfo {
	private Kind kind;
	private ResolvedType type;

	public FastMatchInfo(ResolvedType type, Shadow.Kind kind) {
		this.type = type;
		this.kind = kind;
	}
	
	/**
	 * kind can be null to indicate that all kinds should be considered.
	 * This is usually done as a first pass
	 * @return
	 */
	public Kind getKind() {
		return kind;
	}

	public ResolvedType getType() {
		return type;
	}
	
	public String toString() {
		return "FastMatchInfo [type="+type.getName()+"] ["+(kind==null?"AllKinds":"Kind="+kind)+"]";
	}

}
