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


package org.aspectj.ajdt.internal.compiler.ast;

import java.util.Arrays;

import org.aspectj.ajdt.internal.compiler.lookup.*;
import org.aspectj.ajdt.internal.compiler.lookup.PrivilegedHandler;
import org.aspectj.weaver.*;
import org.aspectj.weaver.ShadowMunger;
import org.eclipse.jdt.internal.compiler.AbstractSyntaxTreeVisitorAdapter;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * Takes a method that already has the three extra parameters
 * thisJoinPointStaticPart, thisJoinPoint and thisEnclosingJoinPointStaticPart
 */

public class MakeDeclsPublicVisitor extends AbstractSyntaxTreeVisitorAdapter {
	
	public void endVisit(
		AnonymousLocalTypeDeclaration decl,
		BlockScope scope) {
		decl.binding.modifiers = AstUtil.makePublic(decl.binding.modifiers);
	}
	
	public void endVisit(LocalTypeDeclaration decl, BlockScope scope) {
		decl.binding.modifiers = AstUtil.makePublic(decl.binding.modifiers);
	}


	public void endVisit(ConstructorDeclaration decl, ClassScope scope) {
		decl.binding.modifiers = AstUtil.makePublic(decl.binding.modifiers);
	}

	public void endVisit(FieldDeclaration decl, MethodScope scope) {
		decl.binding.modifiers = AstUtil.makePublic(decl.binding.modifiers);
	}


	public void endVisit(MethodDeclaration decl, ClassScope scope) {
		decl.binding.modifiers = AstUtil.makePublic(decl.binding.modifiers);
	}

}
