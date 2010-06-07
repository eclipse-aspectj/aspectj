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

import org.aspectj.ajdt.internal.compiler.ast.InterTypeConstructorDeclaration;
import org.aspectj.asm.internal.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodScope;

/**
 * Prints the declaration for an intertype declared constructor.
 * 
 * @author Andy Clement
 */
class ITDConstructorPrinter extends CommonPrinter {

	private InterTypeConstructorDeclaration constructorDeclaration;

	ITDConstructorPrinter(InterTypeConstructorDeclaration constructorDeclaration, MethodScope mscope) {
		super(mscope);
		this.output = new StringBuilder();
		this.declaration = constructorDeclaration;
		this.constructorDeclaration = constructorDeclaration;
	}

	public String print() {
		return print(2);
	}

	public String print(int tab) {

		this.output = new StringBuilder();

		if (constructorDeclaration.javadoc != null) {
			throwit(null);
			// md.javadoc.print(tab, output);
		}
		printIndent(tab);
		if (constructorDeclaration.annotations != null) {
			printAnnotations(constructorDeclaration.annotations);
		}
		printModifiers(constructorDeclaration.declaredModifiers); // not md.modifiers

		TypeParameter[] typeParams = constructorDeclaration.typeParameters();
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

		char[] s = constructorDeclaration.getDeclaredSelector();

		output.append(CharOperation.subarray(s, 0, s.length - 4)).append('(');
		if (constructorDeclaration.arguments != null) {
			for (int i = Modifier.isStatic(constructorDeclaration.declaredModifiers) ? 0 : 1; i < constructorDeclaration.arguments.length; i++) {

				printArgument(constructorDeclaration.arguments[i]);
				if ((i + 1) < constructorDeclaration.arguments.length) {
					output.append(", "); //$NON-NLS-1$
				}
			}
		}
		output.append(')');

		if (constructorDeclaration.thrownExceptions != null) {
			output.append(" throws "); //$NON-NLS-1$
			for (int i = 0; i < constructorDeclaration.thrownExceptions.length; i++) {
				if (i > 0) {
					output.append(", "); //$NON-NLS-1$
				}
				printTypeReference(constructorDeclaration.thrownExceptions[i]);
			}
		}
		printBody(tab + 1);
		return output.toString();
	}

	// TODO better name
	public StringBuilder printReturnType(int indent) {
		if (constructorDeclaration.returnType == null) {
			return output;
		}
		return printExpression(constructorDeclaration.returnType).append(' ');
	}
}