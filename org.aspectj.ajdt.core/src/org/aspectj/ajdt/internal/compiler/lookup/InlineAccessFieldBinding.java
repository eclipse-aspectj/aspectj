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

import java.lang.reflect.Modifier;

import org.aspectj.ajdt.internal.compiler.ast.*;
import org.aspectj.ajdt.internal.compiler.ast.AstUtil;
import org.aspectj.weaver.*;
import org.aspectj.weaver.NameMangler;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.util.CharOperation;


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
	
	public InlineAccessFieldBinding(AspectDeclaration inAspect, FieldBinding baseField) {
		super(baseField, baseField.declaringClass);

		this.reader = new SimpleSyntheticAccessMethodBinding(
			inAspect.world.makeMethodBinding(
				AjcMemberMaker.inlineAccessMethodForFieldGet(
					inAspect.typeX, inAspect.world.makeResolvedMember(baseField)
			)));
		this.writer = new SimpleSyntheticAccessMethodBinding(inAspect.world.makeMethodBinding(
				AjcMemberMaker.inlineAccessMethodForFieldSet(
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


	public String toString() { return "InlineAccess(" + baseField + ")"; }
}
