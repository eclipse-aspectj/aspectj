/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.aspectj.org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;

/**
 * AdviceDeclaration DOM AST node.
 * Source code forked from MethodDeclaration.
 * An abstract AdviceDeclaration is just like a MethodDeclaration,
 * but without a name or return type.
 * @author ajh02
 */

public abstract class AdviceDeclaration extends BodyDeclaration {
	
	/**
	 * The "javadoc" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor JAVADOC_PROPERTY = 
		internalJavadocPropertyFactory(AdviceDeclaration.class);

	/**
	 * The "parameters" structural property of this node type).
	 * @since 3.0
	 */
	public static final ChildListPropertyDescriptor PARAMETERS_PROPERTY = 
		new ChildListPropertyDescriptor(AdviceDeclaration.class, "parameters", SingleVariableDeclaration.class, CYCLE_RISK); //$NON-NLS-1$
	
	public static final ChildPropertyDescriptor POINTCUT_PROPERTY = 
		new ChildPropertyDescriptor(AdviceDeclaration.class, "pointcut", PointcutDesignator.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$
		
	/**
	 * The "thrownExceptions" structural property of this node type).
	 * @since 3.0
	 */
	public static final ChildListPropertyDescriptor THROWN_EXCEPTIONS_PROPERTY = 
		new ChildListPropertyDescriptor(AdviceDeclaration.class, "thrownExceptions", Name.class, NO_CYCLE_RISK); //$NON-NLS-1$
	
	/**
	 * The "body" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor BODY_PROPERTY = 
		new ChildPropertyDescriptor(AdviceDeclaration.class, "body", Block.class, OPTIONAL, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.0
	 */
	protected static List PROPERTY_DESCRIPTORS_2_0;
	
	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.1
	 */
	protected static List PROPERTY_DESCRIPTORS_3_0;
	
	static {
		List propertyList = new ArrayList(5);
		createPropertyList(AdviceDeclaration.class, propertyList);
		addProperty(JAVADOC_PROPERTY, propertyList);
		addProperty(PARAMETERS_PROPERTY, propertyList);
		addProperty(THROWN_EXCEPTIONS_PROPERTY, propertyList);
		addProperty(POINTCUT_PROPERTY, propertyList);
		addProperty(BODY_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_2_0 = reapPropertyList(propertyList);
		
		propertyList = new ArrayList(5);
		createPropertyList(AdviceDeclaration.class, propertyList);
		addProperty(JAVADOC_PROPERTY, propertyList);
		addProperty(PARAMETERS_PROPERTY, propertyList);
		addProperty(THROWN_EXCEPTIONS_PROPERTY, propertyList);
		addProperty(POINTCUT_PROPERTY, propertyList);
		addProperty(BODY_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_3_0 = reapPropertyList(propertyList);
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 * 
	 * @param apiLevel the API level; one of the AST.JLS* constants
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

	/**
	 * The parameter declarations 
	 * (element type: <code>SingleVariableDeclaration</code>).
	 * Defaults to an empty list.
	 */
	protected ASTNode.NodeList parameters =
		new ASTNode.NodeList(PARAMETERS_PROPERTY);
	
	private PointcutDesignator pointcut;

	/**
	 * The list of thrown exception names (element type: <code>Name</code>).
	 * Defaults to an empty list.
	 */
	protected ASTNode.NodeList thrownExceptions =
		new ASTNode.NodeList(THROWN_EXCEPTIONS_PROPERTY);

	/**
	 * The method body, or <code>null</code> if none.
	 * Defaults to none.
	 */
	private Block optionalBody = null;
	
	/**
	 * Creates a new AST node for a method declaration owned 
	 * by the given AST. By default, the declaration is for a method of an
	 * unspecified, but legal, name; no modifiers; no javadoc; no type 
	 * parameters; void return type; no parameters; no array dimensions after 
	 * the parameters; no thrown exceptions; and no body (as opposed to an
	 * empty body).
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	AdviceDeclaration(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 * @since 3.0
	 */
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == JAVADOC_PROPERTY) {
			if (get) {
				return getJavadoc();
			} else {
				setJavadoc((Javadoc) child);
				return null;
			}
		}
		if (property == POINTCUT_PROPERTY) {
			if (get) {
				return getPointcut();
			} else {
				setPointcut((PointcutDesignator) child);
				return null;
			}
		}
		if (property == BODY_PROPERTY) {
			if (get) {
				return getBody();
			} else {
				setBody((Block) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	List internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == PARAMETERS_PROPERTY) {
			return parameters();
		}
		if (property == THROWN_EXCEPTIONS_PROPERTY) {
			return thrownExceptions();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on BodyDeclaration.
	 */
	final ChildPropertyDescriptor internalJavadocProperty() {
		return JAVADOC_PROPERTY;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final int getNodeType0() {
		return METHOD_DECLARATION; // ajh02: should add one like ADVICE_DECLARATION or something
	}
	
	public PointcutDesignator getPointcut(){
		return pointcut;
	}
	public void setPointcut(PointcutDesignator pointcut) {
		if (pointcut == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.pointcut;
		preReplaceChild(oldChild, pointcut, POINTCUT_PROPERTY);
		this.pointcut = pointcut;
		postReplaceChild(oldChild, pointcut, POINTCUT_PROPERTY);
	}
	

	/**
	 * Returns the live ordered list of method parameter declarations for this
	 * method declaration.
	 * 
	 * @return the live list of method parameter declarations
	 *    (element type: <code>SingleVariableDeclaration</code>)
	 */ 
	public List parameters() {
		return this.parameters;
	}
	
	/**
	 * Returns the live ordered list of thrown exception names in this method 
	 * declaration.
	 * 
	 * @return the live list of exception names
	 *    (element type: <code>Name</code>)
	 */ 
	public List thrownExceptions() {
		return this.thrownExceptions;
	}

	/**
	 * Returns the body of this method declaration, or <code>null</code> if 
	 * this method has <b>no</b> body.
	 * <p>
	 * Note that there is a subtle difference between having no body and having
	 * an empty body ("{}").
	 * </p>
	 * 
	 * @return the method body, or <code>null</code> if this method has no
	 *    body
	 */ 
	public Block getBody() {
		return this.optionalBody;
	}

	/**
	 * Sets or clears the body of this method declaration.
	 * <p>
	 * Note that there is a subtle difference between having no body 
	 * (as in <code>"void foo();"</code>) and having an empty body (as in
	 * "void foo() {}"). Abstract methods, and methods declared in interfaces,
	 * have no body. Non-abstract methods, and all constructors, have a body.
	 * </p>
	 * 
	 * @param body the block node, or <code>null</code> if 
	 *    there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setBody(Block body) {
		// a MethodDeclaration may occur in a Block - must check cycles
		ASTNode oldChild = this.optionalBody;
		preReplaceChild(oldChild, body, BODY_PROPERTY);
		this.optionalBody = body;
		postReplaceChild(oldChild, body, BODY_PROPERTY);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 0; // ajh02: stub. I don't know what this is meant to do
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (this.optionalDocComment == null ? 0 : getJavadoc().treeSize())
			+ (this.modifiers == null ? 0 : this.modifiers.listSize())
			+ this.parameters.listSize()
			+ this.thrownExceptions.listSize()
			+ (this.pointcut == null ? 0 : getPointcut().treeSize())
			+ (this.optionalBody == null ? 0 : getBody().treeSize());
	}
	
	final SimplePropertyDescriptor internalModifiersProperty() {
		return internalModifiersPropertyFactory(AdviceDeclaration.class); // ajh02: stub method. I don't know what this does
	}
	final ChildListPropertyDescriptor internalModifiers2Property() {
		return internalModifiers2PropertyFactory(AdviceDeclaration.class);// ajh02: stub method. I don't know what this does
	}
	
}

