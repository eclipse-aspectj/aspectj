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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.problem.AbortType;

public class MemberTypeDeclaration extends InnerTypeDeclaration {
	public TypeDeclaration enclosingType;
	
public MemberTypeDeclaration(CompilationResult compilationResult){
	super(compilationResult);
}		
/**
 *	Iteration for a member innertype
 *
 */
public void traverse(IAbstractSyntaxTreeVisitor visitor, ClassScope classScope) {
	if (ignoreFurtherInvestigation)
		return;
	try {
		if (visitor.visit(this, classScope)) {
			if (superclass != null)
				superclass.traverse(visitor, scope);
			if (superInterfaces != null) {
				int superInterfaceLength = superInterfaces.length;
				for (int i = 0; i < superInterfaceLength; i++)
					superInterfaces[i].traverse(visitor, scope);
			}
			if (memberTypes != null) {
				int memberTypesLength = memberTypes.length;
				for (int i = 0; i < memberTypesLength; i++)
					memberTypes[i].traverse(visitor, scope);
			}
			if (fields != null) {
				int fieldsLength = fields.length;
				for (int i = 0; i < fieldsLength; i++) {
					FieldDeclaration field;
					if ((field = fields[i]).isStatic()) {
						field.traverse(visitor, staticInitializerScope);
					} else {
						field.traverse(visitor, initializerScope);
					}
				}
			}
			if (methods != null) {
				int methodsLength = methods.length;
				for (int i = 0; i < methodsLength; i++)
					methods[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, classScope);
	} catch (AbortType e) {
	}
}
}
