/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
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
