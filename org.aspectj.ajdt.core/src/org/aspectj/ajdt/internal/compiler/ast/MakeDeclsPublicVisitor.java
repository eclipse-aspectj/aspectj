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

import org.eclipse.jdt.internal.compiler.ASTVisitor;
//import org.eclipse.jdt.internal.compiler.ast.AnonymousLocalTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
//import org.eclipse.jdt.internal.compiler.ast.LocalTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;

/**
 * Takes a method that already has the three extra parameters
 * thisJoinPointStaticPart, thisJoinPoint and thisEnclosingJoinPointStaticPart
 */

public class MakeDeclsPublicVisitor extends ASTVisitor {

	public void endVisit(ConstructorDeclaration decl, ClassScope scope) {
		decl.binding.modifiers = AstUtil.makePublic(decl.binding.modifiers);
	}

	public void endVisit(FieldDeclaration decl, MethodScope scope) {
		decl.binding.modifiers = AstUtil.makePublic(decl.binding.modifiers);
	}


	public void endVisit(MethodDeclaration decl, ClassScope scope) {
		decl.binding.modifiers = AstUtil.makePublic(decl.binding.modifiers);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ASTVisitor#endVisit(org.eclipse.jdt.internal.compiler.ast.TypeDeclaration, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public void endVisit(
		TypeDeclaration localTypeDeclaration,
		BlockScope scope) {
		localTypeDeclaration.binding.modifiers = AstUtil.makePublic(localTypeDeclaration.binding.modifiers);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ASTVisitor#endVisit(org.eclipse.jdt.internal.compiler.ast.TypeDeclaration, org.eclipse.jdt.internal.compiler.lookup.ClassScope)
	 */
	public void endVisit(
		TypeDeclaration memberTypeDeclaration,
		ClassScope scope) {
		memberTypeDeclaration.binding.modifiers = AstUtil.makePublic(memberTypeDeclaration.binding.modifiers);
	}

}
