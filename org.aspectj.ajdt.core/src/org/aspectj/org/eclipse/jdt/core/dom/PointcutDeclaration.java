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
 * PointcutDeclaration DOM AST node.
 * has:
 *   a name
 *   an optional pointcut designator called 'designator'
 *   a SingleVariableDeclaration list called 'parameters'
 *   javadoc
 *   modifiers
 *   
 * note:
 *   should also have a property for its parameter list,
 *   like the one MethodDeclarations have.
 * @author ajh02
 */
public class PointcutDeclaration extends BodyDeclaration {
	
	private SimpleName pointcutName = null;
	public static final ChildPropertyDescriptor NAME_PROPERTY = 
		new ChildPropertyDescriptor(PointcutDeclaration.class, "name", SimpleName.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$
	
	private PointcutDesignator pointcutDesignator = null;
	public static final ChildPropertyDescriptor DESIGNATOR_PROPERTY = 
		new ChildPropertyDescriptor(PointcutDeclaration.class, "designator", PointcutDesignator.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "parameters" structural property of this node type.
	 */
	public static final ChildListPropertyDescriptor PARAMETERS_PROPERTY = 
		new ChildListPropertyDescriptor(PointcutDeclaration.class, "parameters", SingleVariableDeclaration.class, CYCLE_RISK); //$NON-NLS-1$
	
	public PointcutDesignator getDesignator() {
		return this.pointcutDesignator;
	}
	
	public void setDesignator(PointcutDesignator pointcutDesignator) {
		ASTNode oldChild = this.pointcutDesignator;
		preReplaceChild(oldChild, pointcutDesignator, DESIGNATOR_PROPERTY);
		this.pointcutDesignator = pointcutDesignator;
		postReplaceChild(oldChild, pointcutDesignator, DESIGNATOR_PROPERTY);
	}
	
	public SimpleName getName() {
		if (this.pointcutName == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.pointcutName == null) {
					preLazyInit();
					this.pointcutName = new SimpleName(this.ast);
					postLazyInit(this.pointcutName, NAME_PROPERTY);
				}
			}
		}
		return this.pointcutName;
	}
	
	public void setName(SimpleName pointcutName) {
		if (pointcutName == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.pointcutName;
		preReplaceChild(oldChild, pointcutName, NAME_PROPERTY);
		this.pointcutName = pointcutName;
		postReplaceChild(oldChild, pointcutName, NAME_PROPERTY);
	}
	
	/**
	 * The "javadoc" structural property of this node type.
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor JAVADOC_PROPERTY = 
		internalJavadocPropertyFactory(PointcutDeclaration.class);

	/**
	 * The "modifiers" structural property of this node type (JLS2 API only).
	 * @since 3.0
	 */
	public static final SimplePropertyDescriptor MODIFIERS_PROPERTY = 
		internalModifiersPropertyFactory(PointcutDeclaration.class);
	
	/**
	 * The "modifiers" structural property of this node type (added in JLS3 API).
	 * @since 3.1
	 */
	public static final ChildListPropertyDescriptor MODIFIERS2_PROPERTY = 
		internalModifiers2PropertyFactory(PointcutDeclaration.class);
		
	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.0
	 */
	private static final List PROPERTY_DESCRIPTORS_2_0;
	
	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.1
	 */
	private static final List PROPERTY_DESCRIPTORS_3_0;
	
	static {
		List propertyList = new ArrayList(6);
		createPropertyList(PointcutDeclaration.class, propertyList);
		addProperty(JAVADOC_PROPERTY, propertyList);
		addProperty(MODIFIERS_PROPERTY, propertyList);
		addProperty(NAME_PROPERTY, propertyList);
		addProperty(DESIGNATOR_PROPERTY, propertyList);
		addProperty(PARAMETERS_PROPERTY, propertyList);
		
		PROPERTY_DESCRIPTORS_2_0 = reapPropertyList(propertyList);
		
		propertyList = new ArrayList(6);
		createPropertyList(PointcutDeclaration.class, propertyList);
		addProperty(JAVADOC_PROPERTY, propertyList);
		addProperty(MODIFIERS2_PROPERTY, propertyList);
		addProperty(NAME_PROPERTY, propertyList);
		addProperty(DESIGNATOR_PROPERTY, propertyList);
		addProperty(PARAMETERS_PROPERTY, propertyList);
		
		PROPERTY_DESCRIPTORS_3_0 = reapPropertyList(propertyList);
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

	/**
	 * Creates a new unparented field declaration statement node owned 
	 * by the given AST.  By default, the field declaration has: no modifiers,
	 * an unspecified (but legal) type, and an empty list of variable 
	 * declaration fragments (which is syntactically illegal).
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	PointcutDeclaration(AST ast) {
		super(ast);
	}
	
	protected ASTNode.NodeList parameters =
		new ASTNode.NodeList(PARAMETERS_PROPERTY);

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
	final int internalGetSetIntProperty(SimplePropertyDescriptor property, boolean get, int value) {
		if (property == MODIFIERS_PROPERTY) {
			if (get) {
				return getModifiers();
			} else {
				internalSetModifiers(value);
				return 0;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetIntProperty(property, get, value);
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
		if (property == NAME_PROPERTY) {
			if (get) {
				return getName();
			} else {
				setName((SimpleName) child);
				return null;
			}
		}
		if (property == DESIGNATOR_PROPERTY) {
			if (get) {
				return getDesignator();
			} else {
				setDesignator((PointcutDesignator) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final List internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == MODIFIERS2_PROPERTY) {
			return modifiers();
		}
		if (property == PARAMETERS_PROPERTY) {
			return parameters();
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
	 * Method declared on BodyDeclaration.
	 */
	final SimplePropertyDescriptor internalModifiersProperty() {
		return MODIFIERS_PROPERTY;
	}

	/* (omit javadoc for this method)
	 * Method declared on BodyDeclaration.
	 */
	final ChildListPropertyDescriptor internalModifiers2Property() {
		return MODIFIERS2_PROPERTY;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final int getNodeType0() {
		return FIELD_DECLARATION; // ajh02: hmmmmmmm.. should make a POINTCUT_DECLARATION thing
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		PointcutDeclaration result = new PointcutDeclaration(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		if (this.ast.apiLevel == AST.JLS2_INTERNAL) {
			result.internalSetModifiers(getModifiers());
		}
		if (this.ast.apiLevel >= AST.JLS3) {
			result.modifiers().addAll(ASTNode.copySubtrees(target, modifiers()));
		}
		result.setName((SimpleName) getName().clone(target));
		if (getDesignator() != null) {
			result.setDesignator((PointcutDesignator) getDesignator().clone(target));
		}
		result.parameters().addAll(
				ASTNode.copySubtrees(target, parameters()));
		return result;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return ((AjASTMatcher)matcher).match(this, other);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		if (visitor instanceof AjASTVisitor) {
			AjASTVisitor ajvis = ((AjASTVisitor)visitor);
			boolean visitChildren = ajvis.visit(this);
			if (visitChildren) {
				// visit children in normal left to right reading order
				acceptChild(ajvis, getJavadoc());
				if (this.ast.apiLevel >= AST.JLS3) {
					acceptChildren(ajvis, this.modifiers);
				}
				acceptChild(ajvis, getName());
				acceptChild(ajvis, getDesignator());
				acceptChildren(visitor, this.parameters);
			}
			ajvis.endVisit(this);
		}
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
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 3 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (this.optionalDocComment == null ? 0 : getJavadoc().treeSize())
			+ (this.pointcutName == null ? 0 : getName().treeSize())
			+ (this.modifiers == null ? 0 : this.modifiers.listSize())
			+ this.parameters.listSize()
			+ (this.pointcutDesignator == null ? 0 : getDesignator().treeSize());
	}
}
