/* *******************************************************************
 * Copyright (c) 2009 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver;

/**
 * Carries an array of unresolved types - will resolve them on demand. Can be used where currently the entire array gets resolved
 * and a ResolvedType array is passed on. Depending on the situation there may not be a need to resolve all the entries so this can
 * perform better. Note: the array elements are resolved in place, so the caller should not be surprised if elements and resolved
 * after the type list has been used.
 * 
 * @author Andy Clement
 */
public class ResolvableTypeList {

	public int length;
	private World world;
	private UnresolvedType[] types;

	public ResolvableTypeList(World world, UnresolvedType[] unresolvedTypes) {
		length = unresolvedTypes.length;
		types = unresolvedTypes;
		this.world = world;
	}

	public ResolvedType getResolved(int nameIndex) {
		UnresolvedType ut = types[nameIndex];
		if (!(ut instanceof ResolvedType)) {
			types[nameIndex] = world.resolve(ut);
			return (ResolvedType) types[nameIndex];
		} else {
			return (ResolvedType) ut;
		}
	}
}
