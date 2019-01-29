/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.lookup;

import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.Constant;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.ResolvedMember;


/**
 * Used for field references within the body of an around advice
 * to force the use of public access methods.  This makes it possible
 * for around advice to be inlined into any shadow to which it applies.
 * 
 * ??? this is very similar to PrivilegedFieldBinding and is somewhat related
 *     to InterTypeFieldBinding.  Maybe they have a common supertype?
 * 
 * @author Jim Hugunin
 */
public class InlineAccessFieldBinding extends FieldBinding {
	public SimpleSyntheticAccessMethodBinding reader;
	public SimpleSyntheticAccessMethodBinding writer;
	
	
	public FieldBinding baseField;
	
	public InlineAccessFieldBinding(AspectDeclaration inAspect, FieldBinding baseField, ResolvedMember resolvedField) {
		super(baseField, baseField.declaringClass);

		this.reader = new SimpleSyntheticAccessMethodBinding(
			inAspect.factory.makeMethodBinding(
				AjcMemberMaker.inlineAccessMethodForFieldGet(
					inAspect.typeX, resolvedField
			)));
		this.writer = new SimpleSyntheticAccessMethodBinding(inAspect.factory.makeMethodBinding(
				AjcMemberMaker.inlineAccessMethodForFieldSet(
					inAspect.typeX, resolvedField
			)));
			
		this.constant = Constant.NotAConstant;
		this.baseField = baseField;
	}


	public boolean canBeSeenBy(TypeBinding receiverType, InvocationSite invocationSite, Scope scope) {
		return true;
	}

	public SyntheticMethodBinding getAccessMethod(boolean isReadAccess) {
		if (isReadAccess) return reader;
		else return writer;
	}
	
	public boolean alwaysNeedsAccessMethod(boolean isReadAccess) { return true; }

	public FieldBinding getFieldBindingForLookup() { return baseField; }


	public String toString() { return "InlineAccess(" + baseField + ")"; }
}
