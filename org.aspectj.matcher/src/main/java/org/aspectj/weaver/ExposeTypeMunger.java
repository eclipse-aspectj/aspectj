/* *******************************************************************
 * Copyright (c) 2007 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement, IBM       initial implementation
 * ******************************************************************/
package org.aspectj.weaver;

/**
 * Special kind of privileged access munger which exposes a type to be public.
 */
public class ExposeTypeMunger extends PrivilegedAccessMunger {

	public ExposeTypeMunger(UnresolvedType typeToExpose) {
		super(new ResolvedMemberImpl(Member.STATIC_INITIALIZATION, typeToExpose, 0, UnresolvedType.VOID, "<clinit>",
				UnresolvedType.NONE), false);
	}

	public String toString() {
		return "ExposeTypeMunger(" + getSignature().getDeclaringType().getName() + ")";
	}

	public String getExposedTypeSignature() {
		return getSignature().getDeclaringType().getSignature();
	}
}
