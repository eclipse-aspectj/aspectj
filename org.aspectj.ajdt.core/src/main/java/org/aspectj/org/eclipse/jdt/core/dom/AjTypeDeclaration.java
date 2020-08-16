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
 * This subtype of TypeDeclaration allows for the extensions that AspectJ
 * has for types - they might be aspects and pointcuts may exist in
 * classes.  This type does not represent an aspect, that is represented
 * by AspectDeclaration, a further subtype of AjTypeDeclaration.
 */
public class AjTypeDeclaration extends TypeDeclaration {
	
	/**
	 * The "aspect" structural property of this node type.
	 * @since 3.0
	 */
	public static final SimplePropertyDescriptor ASPECT_PROPERTY = 
		new SimplePropertyDescriptor(TypeDeclaration.class, "aspect", boolean.class, MANDATORY); //$NON-NLS-1$
	
	protected static List ajPROPERTY_DESCRIPTORS_2_0;
	protected static List ajPROPERTY_DESCRIPTORS_3_0;

	
	// Need to fix up the property lists created during the super's static initializer
	static {
		// Need to fix up the property lists created during the super's static initializer
		List temporary = new ArrayList();
		createPropertyList(TypeDeclaration.class, temporary);
		temporary.addAll(PROPERTY_DESCRIPTORS_2_0);
		addProperty(ASPECT_PROPERTY, temporary);
		ajPROPERTY_DESCRIPTORS_2_0 = reapPropertyList(temporary);
		
		temporary.clear();
		createPropertyList(TypeDeclaration.class, temporary);
		temporary.addAll(PROPERTY_DESCRIPTORS_3_0);
		addProperty(ASPECT_PROPERTY, temporary);
		ajPROPERTY_DESCRIPTORS_3_0 = reapPropertyList(temporary);
	}

	/**
	 * <code>true</code> for an aspect, <code>false</code> for a class or interface.
	 * Defaults to class.
	 */
	private boolean isAspect = false;
	

	/**
	 * Creates a new AST node for a type declaration owned by the given 
	 * AST. By default, the type declaration is for a class of an
	 * unspecified, but legal, name; no modifiers; no javadoc; 
	 * no type parameters; no superclass or superinterfaces; and an empty list
	 * of body declarations.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	public AjTypeDeclaration(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		AjTypeDeclaration result = new AjTypeDeclaration(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		if (this.ast.apiLevel == AST.JLS2_INTERNAL) {
			result.internalSetModifiers(getModifiers());
			result.setSuperclass(
					(Name) ASTNode.copySubtree(target, getSuperclass()));
			result.superInterfaces().addAll(
					ASTNode.copySubtrees(target, superInterfaces()));
		}
		result.setInterface(isInterface());
		result.setAspect(isAspect());
		result.setName((SimpleName) getName().clone(target));
		if (this.ast.apiLevel >= AST.JLS3) {
			result.modifiers().addAll(ASTNode.copySubtrees(target, modifiers()));
			result.typeParameters().addAll(
					ASTNode.copySubtrees(target, typeParameters()));
			result.setSuperclassType(
					(Type) ASTNode.copySubtree(target, getSuperclassType()));
			result.superInterfaceTypes().addAll(
					ASTNode.copySubtrees(target, superInterfaceTypes()));
		}
		result.bodyDeclarations().addAll(
			ASTNode.copySubtrees(target, bodyDeclarations()));
		return result;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
     boolean internalGetSetBooleanProperty(SimplePropertyDescriptor property, boolean get, boolean value) {
		if (property == ASPECT_PROPERTY) {
			if (get) {
				return isAspect();
			} else {
				setAspect(value);
				return false;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetBooleanProperty(property, get, value);
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
 			return ajPROPERTY_DESCRIPTORS_2_0;
 		} else {
 			return ajPROPERTY_DESCRIPTORS_3_0;
 		}
 	}
 	
	/**
	 * Returns whether this type declaration declares a class or an 
	 * aspect.
	 * 
	 * @return <code>true</code> if this is an aspect declaration,
	 *    and <code>false</code> if this is a class or interface declaration
	 */ 
	public boolean isAspect() {
		return this.isAspect;
	}
	
	/**
	 * Sets whether this type declaration declares a class or an 
	 * aspect.
	 * 
	 * @param isAspect <code>true</code> if this is an aspect
	 *    declaration, and <code>false</code> if this is a class or interface
	 * 	  declaration
	 */ 
	public void setAspect(boolean isAspect) {
		preValueChange(ASPECT_PROPERTY);
		this.isAspect = isAspect;
		postValueChange(ASPECT_PROPERTY);
	}

	public PointcutDeclaration[] getPointcuts() {
		// ajh02: method added, currently returning none :-/
		List bd = bodyDeclarations();
		// ajh02: 0 bodyDeclarations :-/
		int pointcutCount = 0;
		for (Object o : bd) {
			if (o instanceof PointcutDeclaration) {
				pointcutCount++;
			}
		}
		PointcutDeclaration[] pointcuts = new PointcutDeclaration[pointcutCount];
		int next = 0;
		for (Object decl : bd) {
			if (decl instanceof PointcutDeclaration) {
				pointcuts[next++] = (PointcutDeclaration) decl;
			}
		}
		return pointcuts;
	}
	
	public ASTNode.NodeList getSuperInterfaceNames() {
		return superInterfaceNames;
	}
	
	public ASTNode.NodeList getTypeParameters() {
		return typeParameters;
	}

}

