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
 * AroundAdviceDeclaration DOM AST node.
 * has:
 *   everything an AdviceDeclaration has,
 *   a return type (or return type Mark2)
 *   
 * It inherits property descriptors from AdviceDeclaration,
 * but needs to add one for its return type,
 * but I can't mix descripters from two different classes in a property list,
 * so I have to redefine them all here and use
 * a 'around' prefix to distinguish them from the ones defined in AdviceDeclaration.
 * There has to be a better way, but this works.
 * @author ajh02
 *
 */

public class AroundAdviceDeclaration extends AdviceDeclaration {
	
	public static final ChildPropertyDescriptor aroundRETURN_TYPE_PROPERTY = 
		new ChildPropertyDescriptor(AroundAdviceDeclaration.class, "returnType", Type.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	public static final ChildPropertyDescriptor aroundRETURN_TYPE2_PROPERTY = 
		new ChildPropertyDescriptor(AroundAdviceDeclaration.class, "returnType2", Type.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$
	
	public static final ChildListPropertyDescriptor aroundTYPE_PARAMETERS_PROPERTY = 
		new ChildListPropertyDescriptor(AroundAdviceDeclaration.class, "typeParameters", TypeParameter.class, NO_CYCLE_RISK); //$NON-NLS-1$
	
	public static final ChildPropertyDescriptor aroundJAVADOC_PROPERTY = 
		internalJavadocPropertyFactory(AroundAdviceDeclaration.class);

	public static final ChildListPropertyDescriptor aroundPARAMETERS_PROPERTY = 
		new ChildListPropertyDescriptor(AroundAdviceDeclaration.class, "parameters", SingleVariableDeclaration.class, CYCLE_RISK); //$NON-NLS-1$
	
	public static final ChildPropertyDescriptor aroundPOINTCUT_PROPERTY = 
		new ChildPropertyDescriptor(AroundAdviceDeclaration.class, "pointcut", PointcutDesignator.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$
		
	public static final ChildListPropertyDescriptor aroundTHROWN_EXCEPTIONS_PROPERTY = 
		new ChildListPropertyDescriptor(AroundAdviceDeclaration.class, "thrownExceptions", Name.class, NO_CYCLE_RISK); //$NON-NLS-1$
	
	public static final ChildPropertyDescriptor aroundBODY_PROPERTY = 
		new ChildPropertyDescriptor(AroundAdviceDeclaration.class, "body", Block.class, OPTIONAL, CYCLE_RISK); //$NON-NLS-1$

	protected static List aroundPROPERTY_DESCRIPTORS_2_0;
	protected static List aroundPROPERTY_DESCRIPTORS_3_0;
	
	static {
		List propertyList = new ArrayList(6);
		createPropertyList(AroundAdviceDeclaration.class, propertyList);
		addProperty(aroundJAVADOC_PROPERTY, propertyList);
		addProperty(aroundRETURN_TYPE_PROPERTY, propertyList);
		addProperty(aroundPARAMETERS_PROPERTY, propertyList);
		addProperty(aroundTHROWN_EXCEPTIONS_PROPERTY, propertyList);
		addProperty(aroundPOINTCUT_PROPERTY, propertyList);
		addProperty(aroundBODY_PROPERTY, propertyList);
		aroundPROPERTY_DESCRIPTORS_2_0 = reapPropertyList(propertyList);
		
		propertyList = new ArrayList(7);
		createPropertyList(AroundAdviceDeclaration.class, propertyList);
		addProperty(aroundJAVADOC_PROPERTY, propertyList);
		addProperty(aroundTYPE_PARAMETERS_PROPERTY, propertyList);
		addProperty(aroundRETURN_TYPE2_PROPERTY, propertyList);
		addProperty(aroundPARAMETERS_PROPERTY, propertyList);
		addProperty(aroundTHROWN_EXCEPTIONS_PROPERTY, propertyList);
		addProperty(aroundPOINTCUT_PROPERTY, propertyList);
		addProperty(aroundBODY_PROPERTY, propertyList);
		aroundPROPERTY_DESCRIPTORS_3_0 = reapPropertyList(propertyList);
	}
	
	
	public static List propertyDescriptors(int apiLevel) {
		if (apiLevel == AST.JLS2_INTERNAL) {
			return aroundPROPERTY_DESCRIPTORS_2_0;
		} else {
			return aroundPROPERTY_DESCRIPTORS_3_0;
		}
	}
	
	private Type returnType = null;
	/**
	 * Indicated whether the return type has been initialized.
	 * @since 3.1
	 */
	private boolean returnType2Initialized = false;
	private ASTNode.NodeList typeParameters = null;
	
	AroundAdviceDeclaration(AST ast) {
		super(ast);
		if (ast.apiLevel >= AST.JLS3) { // ajh02: move to aroundAdvice
			this.typeParameters = new ASTNode.NodeList(aroundTYPE_PARAMETERS_PROPERTY);
		}
	}
	
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == aroundRETURN_TYPE_PROPERTY) {
			if (get) {
				return getReturnType();
			} else {
				setReturnType((Type) child);
				return null;
			}
		}
		if (property == aroundRETURN_TYPE2_PROPERTY) {
			if (get) {
				return getReturnType2();
			} else {
				setReturnType2((Type) child);
				return null;
			}
		}
		return super.internalGetSetChildProperty(property, get, child);
	}
	
	final List internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == aroundTYPE_PARAMETERS_PROPERTY) {
			return typeParameters();
		}
		return super.internalGetChildListProperty(property);
	}
	
	public List typeParameters() {
		// more efficient than just calling unsupportedIn2() to check
		if (this.typeParameters == null) {
			unsupportedIn2();
		}
		return this.typeParameters;
	}
	
	public Type getReturnType() {
		return internalGetReturnType();
	}
	
	/**
	 * Internal synonym for deprecated method. Used to avoid
	 * deprecation warnings.
	 * @since 3.1
	 */
	/*package*/ final Type internalGetReturnType() {
		supportedOnlyIn2();
		if (this.returnType == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.returnType == null) {
					preLazyInit();
					this.returnType = this.ast.newPrimitiveType(PrimitiveType.VOID);
					postLazyInit(this.returnType, aroundRETURN_TYPE_PROPERTY);
				}
			}
		}
		return this.returnType;
	}
	
	public void setReturnType(Type type) {
		internalSetReturnType(type);
	}
	
	/*package*/ void internalSetReturnType(Type type) {
	    supportedOnlyIn2();
		if (type == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.returnType;
		preReplaceChild(oldChild, type, aroundRETURN_TYPE_PROPERTY);
		this.returnType = type;
		postReplaceChild(oldChild, type, aroundRETURN_TYPE_PROPERTY);
	}
	
	public Type getReturnType2() {
	    unsupportedIn2();
		if (this.returnType == null && !this.returnType2Initialized) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.returnType == null && !this.returnType2Initialized) {
					preLazyInit();
					this.returnType = this.ast.newPrimitiveType(PrimitiveType.VOID);
					this.returnType2Initialized = true;
					postLazyInit(this.returnType, aroundRETURN_TYPE2_PROPERTY);
				}
			}
		}
		return this.returnType;
	}
	
	public void setReturnType2(Type type) {
	    unsupportedIn2();
		this.returnType2Initialized = true;
		ASTNode oldChild = this.returnType;
		preReplaceChild(oldChild, type, aroundRETURN_TYPE2_PROPERTY);
		this.returnType = type;
		postReplaceChild(oldChild, type, aroundRETURN_TYPE2_PROPERTY);
	}
	
	ASTNode clone0(AST target) {
		AroundAdviceDeclaration result = new AroundAdviceDeclaration(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		result.parameters().addAll(
			ASTNode.copySubtrees(target, parameters()));
		result.thrownExceptions().addAll(
			ASTNode.copySubtrees(target, thrownExceptions()));
		result.setPointcut(getPointcut());
		result.setBody(
			(Block) ASTNode.copySubtree(target, getBody()));
		return result;
	}
	
	int treeSize() {
		return
			super.treeSize()
			+ (this.typeParameters == null ? 0 : this.typeParameters.listSize())
			+ (this.returnType == null ? 0 : this.returnType.treeSize());
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
			boolean visitChildren = ((AjASTVisitor)visitor).visit(this);
			if (visitChildren) {
				// visit children in normal left to right reading order
				acceptChild(visitor, getJavadoc());
				if (ast.apiLevel == AST.JLS2_INTERNAL) {
					acceptChild(visitor, getReturnType());
				} else {
					acceptChild(visitor, getReturnType2());
				}
				
				acceptChildren(visitor, this.parameters);
				acceptChild(visitor, getPointcut());
				acceptChildren(visitor, this.thrownExceptions);
				acceptChild(visitor, getBody());
			}
			((AjASTVisitor)visitor).endVisit(this);
		}
	}
}
