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

import java.util.List;

public class TypeCategoryTypePattern extends TypePattern {

	private int typeCategory;

	public static final int CLASS = org.aspectj.weaver.patterns.TypeCategoryTypePattern.CLASS;
	public static final int INTERFACE = org.aspectj.weaver.patterns.TypeCategoryTypePattern.INTERFACE;
	public static final int ASPECT = org.aspectj.weaver.patterns.TypeCategoryTypePattern.ASPECT;
	public static final int INNER = org.aspectj.weaver.patterns.TypeCategoryTypePattern.INNER;
	public static final int ANONYMOUS = org.aspectj.weaver.patterns.TypeCategoryTypePattern.ANONYMOUS;
	public static final int ENUM = org.aspectj.weaver.patterns.TypeCategoryTypePattern.ENUM;
	public static final int ANNOTATION = org.aspectj.weaver.patterns.TypeCategoryTypePattern.ANNOTATION;

	/**
	 * 
	 * See the weaver implementation for the type categories.
	 * 
	 * @see org.aspectj.weaver.patterns.TypeCategoryTypePattern
	 * @param ast
	 *            must not be null
	 * @param typeCategory
	 *            as defined in the corresponding weaver node type
	 */
	TypeCategoryTypePattern(AST ast, int typeCategory) {
		super(ast, null);
		this.typeCategory = typeCategory;
	}

	/**
	 * 
	 * See the weaver implementation for the type categories.
	 * 
	 * @see org.aspectj.weaver.patterns.TypeCategoryTypePattern
	 * @return type category
	 */
	public int getTypeCategory() {
		return typeCategory;
	}

	List<?> internalStructuralPropertiesForType(int apiLevel) {
		return null;
	}

	public String getTypePatternExpression() {

		String expression = super.getTypePatternExpression();
		if (expression == null) {
			switch (getTypeCategory()) {
			case CLASS:
				expression = "ClassType";
				break;
			case INNER:
				expression = "InnerType";
				break;
			case INTERFACE:
				expression = "InterfaceType";
				break;
			case ANNOTATION:
				expression = "AnnotationType";
				break;
			case ANONYMOUS:
				expression = "AnonymousType";
				break;
			case ASPECT:
				expression = "AspectType";
				break;
			case ENUM:
				expression = "EnumType";
				break;
			}
			expression = (expression != null) ? "is(" + expression + ")"
					: EMPTY_EXPRESSION;
			setTypePatternExpression(expression);

		}
		return expression;
	}

	ASTNode clone0(AST target) {
		ASTNode cloned = new TypeCategoryTypePattern(target, getTypeCategory());
		cloned.setSourceRange(getStartPosition(), getLength());
		return cloned;
	}

	void accept0(ASTVisitor visitor) {
		if (visitor instanceof AjASTVisitor) {
			AjASTVisitor ajVisitor = (AjASTVisitor) visitor;
			ajVisitor.visit(this);
			ajVisitor.endVisit(this);
		}
	}

	boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		if (matcher instanceof AjASTMatcher) {
			AjASTMatcher ajmatcher = (AjASTMatcher) matcher;
			return ajmatcher.match(this, other);
		}
		return false;
	}

}
