/* *******************************************************************
 * Copyright (c) 2010 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement - SpringSource
 * ******************************************************************/
package org.aspectj.ajdt.internal.compiler;

import org.aspectj.ajdt.internal.compiler.ast.InterTypeFieldDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodScope;

/**
 * Prints the declaration for an intertype declared field, including initializer.
 * 
 * @author Andy Clement
 */
class ITDFieldPrinter extends CommonPrinter {

	private InterTypeFieldDeclaration fieldDeclaration;

	ITDFieldPrinter(InterTypeFieldDeclaration fieldDeclaration, MethodScope methodscope) {
		super(methodscope);
		output = new StringBuilder();
		this.fieldDeclaration = fieldDeclaration;
	}

	public String print() {
		return print(2);
	}

	public String print(int tab) {
		this.output = new StringBuilder();

		if (fieldDeclaration.javadoc != null) {
			// TODO javadoc
			// md.javadoc.print(tab, output);
		}
		printIndent(tab);
		if (fieldDeclaration.annotations != null) {
			printAnnotations(fieldDeclaration.annotations);
		}
		printModifiers(fieldDeclaration.declaredModifiers); // not md.modifiers

		TypeParameter[] typeParams = fieldDeclaration.typeParameters();
		if (typeParams != null) {
			output.append('<');
			int max = typeParams.length - 1;
			for (int j = 0; j < max; j++) {
				printTypeParameter(typeParams[j]);
				output.append(", ");//$NON-NLS-1$
			}
			printTypeParameter(typeParams[max]);
			output.append('>');
		}
		printReturnType(0).append(fieldDeclaration.getDeclaredSelector());
		if (fieldDeclaration.initialization != null) {
			output.append(" = ");
			printExpression(fieldDeclaration.initialization);
		}
		output.append(';');
		return output.toString();
	}

	// TODO better name
	public StringBuilder printReturnType(int indent) {
		if (fieldDeclaration.returnType == null) {
			return output;
		}
		return printExpression(fieldDeclaration.returnType).append(' ');
	}
}