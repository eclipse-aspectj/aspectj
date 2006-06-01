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


package org.aspectj.ajdt.internal.compiler.ast;

import org.aspectj.org.eclipse.jdt.internal.compiler.ASTVisitor;
//import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AnonymousLocalTypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
//import org.aspectj.org.eclipse.jdt.internal.compiler.ast.LocalTypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodScope;

/**
 * Takes a method that already has the three extra parameters
 * thisJoinPointStaticPart, thisJoinPoint and thisEnclosingJoinPointStaticPart
 */

public class MakeDeclsPublicVisitor extends ASTVisitor {

	public void endVisit(ConstructorDeclaration decl, ClassScope scope) {
		if (decl.binding==null) return; 
		decl.binding.modifiers = AstUtil.makePublic(decl.binding.modifiers);
	}

	public void endVisit(FieldDeclaration decl, MethodScope scope) {
		if (decl.binding==null) return; 
		decl.binding.modifiers = AstUtil.makePublic(decl.binding.modifiers);
	}


	public void endVisit(MethodDeclaration decl, ClassScope scope) {
		if (decl.binding==null) return; 
		decl.binding.modifiers = AstUtil.makePublic(decl.binding.modifiers);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ASTVisitor#endVisit(org.eclipse.jdt.internal.compiler.ast.TypeDeclaration, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public void endVisit(
		TypeDeclaration localTypeDeclaration,
		BlockScope scope) {
		if (localTypeDeclaration.binding==null) return; 
		localTypeDeclaration.binding.modifiers = AstUtil.makePublic(localTypeDeclaration.binding.modifiers);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ASTVisitor#endVisit(org.eclipse.jdt.internal.compiler.ast.TypeDeclaration, org.eclipse.jdt.internal.compiler.lookup.ClassScope)
	 */
	public void endVisit(
		TypeDeclaration memberTypeDeclaration,
		ClassScope scope) {
		if (memberTypeDeclaration.binding==null) return; 
		memberTypeDeclaration.binding.modifiers = AstUtil.makePublic(memberTypeDeclaration.binding.modifiers);
	}

}
