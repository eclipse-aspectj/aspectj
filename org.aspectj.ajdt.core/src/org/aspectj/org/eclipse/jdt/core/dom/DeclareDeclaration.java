
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
import org.aspectj.weaver.patterns.Declare;

/**
 * DeclareDeclaration DOM AST node.
 * has:
 *   javadoc
 *   
 * This class is a stub and should eventually be made abstract
 * when concrete subclasses exist for all the different types of declare
 * statements in AspectJ; declare warning, error, parents and precedence.
 * @author ajh02
 */

public class DeclareDeclaration extends BodyDeclaration {
	public static final ChildPropertyDescriptor JAVADOC_PROPERTY = 
		internalJavadocPropertyFactory(DeclareDeclaration.class);

	private static final List PROPERTY_DESCRIPTORS_2_0;
	
	private static final List PROPERTY_DESCRIPTORS_3_0;
	
	static {
		List propertyList = new ArrayList(1);
		createPropertyList(DeclareDeclaration.class, propertyList);
		addProperty(JAVADOC_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_2_0 = reapPropertyList(propertyList);
		
		propertyList = new ArrayList(1);
		createPropertyList(DeclareDeclaration.class, propertyList);
		addProperty(JAVADOC_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_3_0 = reapPropertyList(propertyList);
	}

	public static List propertyDescriptors(int apiLevel) {
		if (apiLevel == AST.JLS2_INTERNAL) {
			return PROPERTY_DESCRIPTORS_2_0;
		} else {
			return PROPERTY_DESCRIPTORS_3_0;
		}
	}
	
	public Declare declareDecl;
	DeclareDeclaration(AST ast, Declare declareDecl) {
		super(ast);
		
		System.err.println("DeclareDeclaration constructed.."); // ajh02: line added
		this.declareDecl = declareDecl;
		
	}
	
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}
	
	
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == JAVADOC_PROPERTY) {
			if (get) {
				return getJavadoc();
			} else {
				setJavadoc((Javadoc) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}
	
	
	final ChildPropertyDescriptor internalJavadocProperty() {
		return JAVADOC_PROPERTY;
	}

	final int getNodeType0() {
		return METHOD_DECLARATION; // ajh02: should add one called DECLARE_DECLARATION or something
	}

	ASTNode clone0(AST target) {
		DeclareDeclaration result = new DeclareDeclaration(target,declareDecl);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
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
		boolean visitChildren = ((AjASTVisitor)visitor).visit(this);
		if (visitChildren) {
			// visit children in normal left to right reading order
			acceptChild(visitor, getJavadoc());
		}
		((AjASTVisitor)visitor).endVisit(this);
	}

	/**
	 * Resolves and returns the binding for the method or constructor declared
	 * in this method or constructor declaration.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the binding, or <code>null</code> if the binding cannot be 
	 *    resolved
	 */	
	public IMethodBinding resolveBinding() {
		return null; // ajh02: :-/
		//return this.ast.getBindingResolver().resolveMethod(this);
	}

	int memSize() {
		return super.memSize() + 9 * 4;
	}
	
	int treeSize() {
		return
			memSize()
			+ (this.optionalDocComment == null ? 0 : getJavadoc().treeSize());
	}
	
	final SimplePropertyDescriptor internalModifiersProperty() {
		return internalModifiersPropertyFactory(AdviceDeclaration.class); // ajh02: stub method. I don't know what this does
	}
	final ChildListPropertyDescriptor internalModifiers2Property() {
		return internalModifiers2PropertyFactory(AdviceDeclaration.class);// ajh02: stub method. I don't know what this does
	}
}