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
package org.aspectj.weaver.reflect;

import org.aspectj.weaver.UnresolvedType;

/**
 * Under JDK 1.4 or lower, we can't give generic signature info...
 */
public class Java14GenericSignatureInformationProvider implements
		GenericSignatureInformationProvider {

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.reflect.GenericSignatureInformationProvider#getGenericParameterTypes(org.aspectj.weaver.reflect.ReflectionBasedResolvedMemberImpl)
	 */
	public UnresolvedType[] getGenericParameterTypes(
			ReflectionBasedResolvedMemberImpl resolvedMember) {
		return resolvedMember.getParameterTypes();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.reflect.GenericSignatureInformationProvider#getGenericReturnType(org.aspectj.weaver.reflect.ReflectionBasedResolvedMemberImpl)
	 */
	public UnresolvedType getGenericReturnType(
			ReflectionBasedResolvedMemberImpl resolvedMember) {
		return resolvedMember.getReturnType();
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.reflect.GenericSignatureInformationProvider#isBridge()
	 */
	public boolean isBridge(ReflectionBasedResolvedMemberImpl resolvedMember) {
		return false;
	}
	

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.reflect.GenericSignatureInformationProvider#isVarArgs()
	 */
	public boolean isVarArgs(ReflectionBasedResolvedMemberImpl resolvedMember) {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.reflect.GenericSignatureInformationProvider#isSynthetic()
	 */
	public boolean isSynthetic(ReflectionBasedResolvedMemberImpl resolvedMember) {
		return false;
	}

}
