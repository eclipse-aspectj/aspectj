/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.lookup;

import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.weaver.AjcMemberMaker;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticAccessMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class PrivilegedFieldBinding extends FieldBinding {
	public SimpleSyntheticAccessMethodBinding reader;
	public SimpleSyntheticAccessMethodBinding writer;
	
	
	public FieldBinding baseField;
	
	public PrivilegedFieldBinding(AspectDeclaration inAspect, FieldBinding baseField) {
		super(baseField, baseField.declaringClass);

		this.reader = new SimpleSyntheticAccessMethodBinding(
			inAspect.world.makeMethodBinding(
				AjcMemberMaker.privilegedAccessMethodForFieldGet(
					inAspect.typeX, inAspect.world.makeResolvedMember(baseField)
			)));
		this.writer = new SimpleSyntheticAccessMethodBinding(inAspect.world.makeMethodBinding(
				AjcMemberMaker.privilegedAccessMethodForFieldSet(
					inAspect.typeX, inAspect.world.makeResolvedMember(baseField)
			)));
			
		this.constant = AstNode.NotAConstant;
		this.baseField = baseField;
	}


	public boolean canBeSeenBy(TypeBinding receiverType, InvocationSite invocationSite, Scope scope) {
		return true;

	}


	public SyntheticAccessMethodBinding getAccessMethod(boolean isReadAccess) {
		if (isReadAccess) return reader;
		else return writer;
	}
	
	public boolean alwaysNeedsAccessMethod(boolean isReadAccess) { return true; }

	public FieldBinding getFieldBindingForLookup() { return baseField; }


	public String toString() { return "PrivilegedWrapper(" + baseField + ")"; }
//	public ReferenceBinding getTargetType() {
//		return introducedField.declaringClass;
//	}

}
