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
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.*;

public class LocalTypeDeclaration extends InnerTypeDeclaration {
	public AbstractMethodDeclaration enclosingMethod;

public LocalTypeDeclaration(CompilationResult compilationResult){
	super(compilationResult);
}	

/**
 *	Iteration for a local innertype
 *
 */
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {
	if (ignoreFurtherInvestigation)
		return;
	try {
		if (visitor.visit(this, blockScope)) {
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
						// local type cannot have static fields
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
		visitor.endVisit(this, blockScope);
	} catch (AbortType e) {
	}
}
}
