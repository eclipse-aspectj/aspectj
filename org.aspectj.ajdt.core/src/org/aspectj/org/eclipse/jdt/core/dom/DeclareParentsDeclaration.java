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
 * DeclareParentsDeclaration DOM AST node.
 * 
 * Has everything a DeclareDeclaration has plus:
 *      a TypePattern called 'childTypePattern'
 *      a boolean called 'isExtends'
 *   	a TypePattern list called 'typePatternsList'
 */
public class DeclareParentsDeclaration extends DeclareDeclaration {

	public static final ChildPropertyDescriptor JAVADOC_PROPERTY = 
		internalJavadocPropertyFactory(DeclareParentsDeclaration.class);

	public static final ChildPropertyDescriptor CHILD_TYPE_PATTERN_PROPERTY = 
		new ChildPropertyDescriptor(DeclareParentsDeclaration.class, "childTypePattern", TypePattern.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	public static final SimplePropertyDescriptor IS_EXTENDS_PROPERTY = 
		new SimplePropertyDescriptor(DeclareParentsDeclaration.class, "isExtends", boolean.class, MANDATORY); //$NON-NLS-1$
	
	public static final ChildListPropertyDescriptor PARENTS_TYPE_PATTERNS_LIST_PROPERTY = 
		new ChildListPropertyDescriptor(DeclareParentsDeclaration.class, "typePatternsList", TypePattern.class, NO_CYCLE_RISK); //$NON-NLS-1$
	
	private static final List PROPERTY_DESCRIPTORS_2_0;	
	private static final List PROPERTY_DESCRIPTORS_3_0;
	
	static {
		List propertyList = new ArrayList(4);
		createPropertyList(DeclareParentsDeclaration.class, propertyList);
		addProperty(JAVADOC_PROPERTY, propertyList);
		addProperty(CHILD_TYPE_PATTERN_PROPERTY, propertyList);
		addProperty(IS_EXTENDS_PROPERTY, propertyList);
		addProperty(PARENTS_TYPE_PATTERNS_LIST_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_2_0 = reapPropertyList(propertyList);
		
		propertyList = new ArrayList(4);
		createPropertyList(DeclareParentsDeclaration.class, propertyList);
		addProperty(JAVADOC_PROPERTY, propertyList);
		addProperty(CHILD_TYPE_PATTERN_PROPERTY, propertyList);
		addProperty(IS_EXTENDS_PROPERTY, propertyList);
		addProperty(PARENTS_TYPE_PATTERNS_LIST_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_3_0 = reapPropertyList(propertyList);
	}

	private boolean isExtends;
	private TypePattern childTypePattern;
	protected ASTNode.NodeList parentTypePatterns =new ASTNode.NodeList(PARENTS_TYPE_PATTERNS_LIST_PROPERTY);

	
	DeclareParentsDeclaration(AST ast) {
		this(ast,false);
	}
	
	DeclareParentsDeclaration(AST ast, boolean isExtends) {
		super(ast);
		this.isExtends = isExtends;
	}
	
	ASTNode clone0(AST target) {
		DeclareParentsDeclaration result = new DeclareParentsDeclaration(target/*,declareDecl*/);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		result.setChildTypePattern(
				(TypePattern) ASTNode.copySubtree(target, getChildTypePattern()));
		result.setExtends(isExtends());
		result.parentTypePatterns().addAll(
				ASTNode.copySubtrees(target, parentTypePatterns()));
		return result;
	}

	final boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return ((AjASTMatcher)matcher).match(this, other);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		if (visitor instanceof AjASTVisitor) {
			boolean visitChildren = ((AjASTVisitor)visitor).visit(this);
			if (visitChildren) {
				// visit children in normal left to right reading order
				acceptChild(visitor, getJavadoc());
				acceptChild(visitor, getChildTypePattern());
				acceptChildren(visitor, this.parentTypePatterns);
			}
			((AjASTVisitor)visitor).endVisit(this);
		}
	}

	/* (omit javadoc for this method)
	 * Method declared on BodyDeclaration.
	 * 
	 * There are no modifiers declared for DeclareErrorDeclaration - 
	 * therefore we don't do anything with this
	 */
	SimplePropertyDescriptor internalModifiersProperty() {
		return internalModifiersPropertyFactory(DeclareErrorDeclaration.class);
	}

	/* (omit javadoc for this method)
	 * Method declared on BodyDeclaration.
	 * 
	 * There are no modifiers declared for DeclareErrorDeclaration - 
	 * therefore we don't do anything with this
	 */
	ChildListPropertyDescriptor internalModifiers2Property() {
		return internalModifiers2PropertyFactory(DeclareErrorDeclaration.class);
	}

	/* (omit javadoc for this method)
	 * Method declared on BodyDeclaration.
	 */
	ChildPropertyDescriptor internalJavadocProperty() {
		return JAVADOC_PROPERTY;
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 * 
	 * @param apiLevel the API level; one of the
	 * <code>AST.JLS&ast;</code> constants
	 * @return a list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor})
	 * @since 3.0
	 */
	public static List propertyDescriptors(int apiLevel) {
		if (apiLevel == AST.JLS2_INTERNAL) {
			return PROPERTY_DESCRIPTORS_2_0;
		} else {
			return PROPERTY_DESCRIPTORS_3_0;
		}
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == JAVADOC_PROPERTY) {
			if (get) {
				return getJavadoc();
			} else {
				setJavadoc((Javadoc) child);
				return null;
			}
		}
		if (property == CHILD_TYPE_PATTERN_PROPERTY) {
			if (get) {
				return getChildTypePattern();
			} else {
				setChildTypePattern((TypePattern) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	boolean internalGetSetBooleanProperty(SimplePropertyDescriptor property, boolean get, boolean value) {
		if (property == IS_EXTENDS_PROPERTY) {
			if (get) {
				return isExtends();
			} else {
				setExtends(value);
				return false;
			}
		}
		return super.internalGetSetBooleanProperty(property,get,value);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	List internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == PARENTS_TYPE_PATTERNS_LIST_PROPERTY) {
			return parentTypePatterns();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}
	
	/**
	 * Returns the live ordered list of parent type patterns for this
	 * declare precedence.
	 * 
	 * @return the live list of parent type patterns
	 *    (element type: <code>TypePattern</code>)
	 */ 
	public List parentTypePatterns() {
		return this.parentTypePatterns;
	}
	
	
	public TypePattern getChildTypePattern(){
		return childTypePattern;
	}
	
	public void setChildTypePattern(TypePattern typePattern) {
		if (typePattern == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.childTypePattern;
		preReplaceChild(oldChild, typePattern, CHILD_TYPE_PATTERN_PROPERTY);
		this.childTypePattern = typePattern;
		postReplaceChild(oldChild, typePattern, CHILD_TYPE_PATTERN_PROPERTY);
	}

	/**
	 * Returns whether this declareParents declares an extends
	 * or implements.
	 * 
	 * @return <code>true</code> if this is an extends declaration,
	 *    and <code>false</code> if this is an implements declaration
	 */ 
	public boolean isExtends() {
		return this.isExtends;
	}
	
	/**
	 * Sets whether this declareParents declares an extends or implements.
	 * 
	 * @param isExtends <code>true</code> for an extends declaration,
	 *    and <code>false</code> for an implements declaration
	 */ 
	public void setExtends(boolean isExtends) {
		preValueChange(IS_EXTENDS_PROPERTY);
		this.isExtends = isExtends;
		postValueChange(IS_EXTENDS_PROPERTY);
	}
	
}
