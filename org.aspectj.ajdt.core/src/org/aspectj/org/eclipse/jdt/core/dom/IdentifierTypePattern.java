/********************************************************************
 * Copyright (c) 2010 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: Nieraj Singh - initial implementation
 *******************************************************************/
package org.aspectj.org.eclipse.jdt.core.dom;

public abstract class IdentifierTypePattern extends TypePattern {

	private Type type;

	IdentifierTypePattern(AST ast, Type type) {
		super(ast);
		this.type = type;
		setTypePatternExpression(generateTypePatternExpression(this.type));
	}

	/**
	 * This may be null if no Type has been resolved. A String representation
	 * may still exist.
	 * 
	 * @return type if defined or resolved, or null if not defined or resolved
	 *         at the time when this node is created
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Generate an expression (String representation) for the given type.
	 * 
	 * @param type
	 * @return non-null expression for the given type. Null if no expression can
	 *         be generated.
	 */
	protected String generateTypePatternExpression(Type type) {
		String typeExpression = null;
		if (type instanceof SimpleType) {
			Name name = ((SimpleType) type).getName();
			if (name instanceof SimpleName) {
				typeExpression = ((SimpleName) name).getIdentifier();
			}
		}

		// If expression hasn't been resolved yet, get the toString
		// representation
		if (typeExpression == null && type != null) {
			typeExpression = type.toString();
		}

		return typeExpression;
	}

	int memSize() {

		int memSize = super.memSize();

		Type type = getType();
		if (type != null) {
			memSize += type.memSize();
		}

		return memSize;
	}

	void accept0(ASTVisitor visitor) {
		if (visitor instanceof AjASTVisitor) {
			AjASTVisitor ajVisitor = (AjASTVisitor) visitor;
			boolean visited = ajVisitor.visit(this);
			Type type = getType();
			if (visited && type != null) {
				ajVisitor.visit(type);
			}
			ajVisitor.endVisit(this);
		}
	}

}
