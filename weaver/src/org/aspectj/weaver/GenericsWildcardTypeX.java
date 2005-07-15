/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver;

/**
 * Represents the ? type in a generics signature prior to resolving.
 *
 */
public class GenericsWildcardTypeX extends UnresolvedType {

	public static final GenericsWildcardTypeX GENERIC_WILDCARD = 
		new GenericsWildcardTypeX();
	
	private BoundedReferenceType resolved = null;
	
	private GenericsWildcardTypeX() {
		super("Ljava/lang/Object;");  // should be super("?") ?
	}
	
	public ResolvedType resolve(World world) {
		if (resolved == null) {
			resolved = new BoundedReferenceType("Ljava/lang/Object;",world);
		}
		return resolved;
	}

}
