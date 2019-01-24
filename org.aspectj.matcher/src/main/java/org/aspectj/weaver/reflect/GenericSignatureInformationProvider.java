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
 * This interface exists to support two different strategies for answering 
 * generic signature related questions on Java 5 and pre-Java 5.
 */
public interface GenericSignatureInformationProvider {

	UnresolvedType[]  getGenericParameterTypes(ReflectionBasedResolvedMemberImpl resolvedMember);
	
	UnresolvedType    getGenericReturnType(ReflectionBasedResolvedMemberImpl resolvedMember);

	boolean isBridge(ReflectionBasedResolvedMemberImpl resolvedMember);
	
	boolean isVarArgs(ReflectionBasedResolvedMemberImpl resolvedMember);
	
	boolean isSynthetic(ReflectionBasedResolvedMemberImpl resolvedMember);
}
