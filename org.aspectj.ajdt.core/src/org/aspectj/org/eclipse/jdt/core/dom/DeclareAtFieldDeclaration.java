/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 *******************************************************************/
package org.aspectj.org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;

/**
 * DeclareAtFieldDeclaration DOM AST node.
 * 
 * Has everything a DeclareDeclaration has.
 *      
 * Unsupported for JLS2.    
 */
public class DeclareAtFieldDeclaration extends DeclareAnnotationDeclaration {

	public static final ChildPropertyDescriptor JAVADOC_PROPERTY = 
		internalJavadocPropertyFactory(DeclareAtFieldDeclaration.class);

	public static final ChildPropertyDescriptor PATTERN_PROPERTY = 
		internalPatternNodePropertyFactory(DeclareAtFieldDeclaration.class);

	public static final ChildPropertyDescriptor ANNOTATION_NAME_PROPERTY = 
		internalAnnotationNamePropertyFactory(DeclareAtFieldDeclaration.class);

	private static final List PROPERTY_DESCRIPTORS;

	static {
		List propertyList = new ArrayList(3);
		createPropertyList(DeclareAtFieldDeclaration.class, propertyList);
		addProperty(JAVADOC_PROPERTY, propertyList);
		addProperty(PATTERN_PROPERTY, propertyList);
		addProperty(ANNOTATION_NAME_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(propertyList);
	}

	DeclareAtFieldDeclaration(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		DeclareAtFieldDeclaration result = new DeclareAtFieldDeclaration(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setJavadoc((Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		result.setPatternNode((PatternNode) ASTNode.copySubtree(target,
				getPatternNode()));
		result.setAnnotationName((SimpleName) ASTNode.copySubtree(target,
				getAnnotationName()));
		return result;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return ((AjASTMatcher) matcher).match(this, other);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		if (visitor instanceof AjASTVisitor) {
			boolean visitChildren = ((AjASTVisitor) visitor).visit(this);
			if (visitChildren) {
				// visit children in normal left to right reading order
				acceptChild(visitor, getJavadoc());
				acceptChild(visitor, getPatternNode());
				acceptChild(visitor, getAnnotationName());
			}
			((AjASTVisitor) visitor).endVisit(this);
		}
	}

	/*
	 * (omit javadoc for this method) Method declared on BodyDeclaration.
	 * 
	 * There are no modifiers declared for DeclareErrorDeclaration - therefore
	 * we don't do anything with this
	 */
	SimplePropertyDescriptor internalModifiersProperty() {
		return internalModifiersPropertyFactory(DeclareErrorDeclaration.class);
	}

	/*
	 * (omit javadoc for this method) Method declared on BodyDeclaration.
	 * 
	 * There are no modifiers declared for DeclareErrorDeclaration - therefore
	 * we don't do anything with this
	 */
	ChildListPropertyDescriptor internalModifiers2Property() {
		return internalModifiers2PropertyFactory(DeclareErrorDeclaration.class);
	}

	/*
	 * (omit javadoc for this method) Method declared on BodyDeclaration.
	 */
	ChildPropertyDescriptor internalJavadocProperty() {
		return JAVADOC_PROPERTY;
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 * 
	 * @param apiLevel
	 *            the API level; one of the <code>AST.JLS&ast;</code>
	 *            constants
	 * @return a list of property descriptors (element type:
	 *         {@link StructuralPropertyDescriptor})
	 * @since 3.0
	 */
	public static List propertyDescriptors(int apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}

	/*
	 * (omit javadoc for this method) Method declared on ASTNode.
	 */
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}

	/*
	 * (omit javadoc for this method) Method declared on ASTNode.
	 */
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property,
			boolean get, ASTNode child) {
		if (property == JAVADOC_PROPERTY) {
			if (get) {
				return getJavadoc();
			} else {
				setJavadoc((Javadoc) child);
				return null;
			}
		}
		if (property == PATTERN_PROPERTY) {
			if (get) {
				return getPatternNode();
			} else {
				setPatternNode((PatternNode) child);
				return null;
			}
		}
		if (property == ANNOTATION_NAME_PROPERTY) {
			if (get) {
				return getAnnotationName();
			} else {
				setAnnotationName((SimpleName) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	/* (omit javadoc for this method)
	 * Method declared on DeclareAnnotationDeclaration.
	 */
	ChildPropertyDescriptor internalPatternNodeProperty() {
		return PATTERN_PROPERTY;
	}

	/* (omit javadoc for this method)
	 * Method declared on DeclareAnnotationDeclaration.
	 */
	ChildPropertyDescriptor internalAnnotationNameProperty() {
		return ANNOTATION_NAME_PROPERTY;
	}

}
