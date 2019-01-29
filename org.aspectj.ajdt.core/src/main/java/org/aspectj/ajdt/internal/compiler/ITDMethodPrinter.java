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

import java.lang.reflect.Modifier;

import org.aspectj.ajdt.internal.compiler.ast.InterTypeMethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodScope;

/**
 * Prints the declaration for an intertype declared method.
 * 
 * @author Andy Clement
 */
class ITDMethodPrinter extends CommonPrinter {
	private InterTypeMethodDeclaration methodDeclaration;

	ITDMethodPrinter(InterTypeMethodDeclaration methodDeclaration, MethodScope methodscope) {
		super(methodscope);
		output = new StringBuilder();
		this.methodDeclaration = methodDeclaration;
		this.declaration = methodDeclaration;
	}

	public String print() {
		return print(2);
	}

	public StringBuilder printReturnType(int indent) {
		if (methodDeclaration.returnType == null) {
			return output;
		}
		return printExpression(methodDeclaration.returnType).append(' ');
	}

	public String print(int tab) {
		this.output = new StringBuilder();

		if (methodDeclaration.javadoc != null) {
			// TODO javadoc support...
			// md.javadoc.print(tab, output);
		}
		printIndent(tab);
		if (methodDeclaration.annotations != null) {
			printAnnotations(methodDeclaration.annotations);
		}
		printModifiers(methodDeclaration.declaredModifiers); // not md.modifiers

		TypeParameter[] typeParams = methodDeclaration.typeParameters();
		if (typeParams != null) {
			output.append('<');
			int max = typeParams.length - 1;
			for (int j = 0; j < max; j++) {
				printTypeParameter(typeParams[j]);
				output.append(", ");//$NON-NLS-1$
			}
			printTypeParameter(typeParams[max]);
			output.append("> ");
		}

		printReturnType(0).append(methodDeclaration.getDeclaredSelector()).append('(');
		if (methodDeclaration.arguments != null) {
			for (int i = Modifier.isStatic(methodDeclaration.declaredModifiers) ? 0 : 1; i < methodDeclaration.arguments.length; i++) {

				printArgument(methodDeclaration.arguments[i]);
				if ((i + 1) < methodDeclaration.arguments.length) {
					output.append(", "); //$NON-NLS-1$
				}
			}
		}
		output.append(')');

		if (methodDeclaration.thrownExceptions != null) {
			output.append(" throws "); //$NON-NLS-1$
			for (int i = 0; i < methodDeclaration.thrownExceptions.length; i++) {
				if (i > 0) {
					output.append(", "); //$NON-NLS-1$
				}
				printTypeReference(methodDeclaration.thrownExceptions[i]);
			}
		}
		printBody(tab + 1);
		return output.toString();
	}
}