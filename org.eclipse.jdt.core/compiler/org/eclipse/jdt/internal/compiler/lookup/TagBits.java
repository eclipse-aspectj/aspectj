/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

public interface TagBits {
	// Tag bits in the tagBits int of every TypeBinding
	final int IsArrayType = 0x0001;
	final int IsBaseType = 0x0002;
	final int IsNestedType = 0x0004;
	final int IsMemberType = 0x0008;
	final int MemberTypeMask = IsNestedType | IsMemberType;
	final int IsLocalType = 0x0010;
	final int LocalTypeMask = IsNestedType | IsLocalType;
	final int IsAnonymousType = 0x0020;
	final int AnonymousTypeMask = LocalTypeMask | IsAnonymousType;
	final int IsBinaryBinding = 0x0040;

	// for the type hierarchy check used by ClassScope
	final int BeginHierarchyCheck = 0x0100;
	final int EndHierarchyCheck = 0x0200;

	// test bit to see if default abstract methods were computed
	final int KnowsDefaultAbstractMethods = 0x0400;

	// Reusable bit currently used by Scopes
	final int InterfaceVisited = 0x0800;

	// test bits to see if parts of binary types are faulted
	final int AreFieldsComplete = 0x1000;
	final int AreMethodsComplete = 0x2000;

	// test bit to avoid asking a type for a member type (includes inherited member types)
	final int HasNoMemberTypes = 0x4000;

	// test bit to identify if the type's hierarchy is inconsistent
	final int HierarchyHasProblems = 0x8000;
}
