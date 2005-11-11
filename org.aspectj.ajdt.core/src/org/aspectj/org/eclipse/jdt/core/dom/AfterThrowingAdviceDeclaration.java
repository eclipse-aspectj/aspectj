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
 * AfterThrowingAdviceDeclaration DOM AST node.
 * has:
 *   everything an AfterAdviceDeclaration has,
 *   an optional throwing property
 *   
 * It inherits property descriptors from AdviceDeclaration,
 * but needs to add one for its throwing property,
 * but I can't mix descripters from two different classes in a property list,
 * so I have to redefine them all here and use
 * a 'throwing' prefix to distinguish them from the ones defined in AdviceDeclaration.
 * There has to be a better way, but this works.
 * @author ajh02
 *
 */
public class AfterThrowingAdviceDeclaration extends AfterAdviceDeclaration {
	
	public static final ChildPropertyDescriptor throwingJAVADOC_PROPERTY = 
		internalJavadocPropertyFactory(AfterThrowingAdviceDeclaration.class);

	public static final ChildListPropertyDescriptor throwingPARAMETERS_PROPERTY = 
		new ChildListPropertyDescriptor(AfterThrowingAdviceDeclaration.class, "parameters", SingleVariableDeclaration.class, CYCLE_RISK); //$NON-NLS-1$
	
	public static final ChildPropertyDescriptor throwingPOINTCUT_PROPERTY = 
		new ChildPropertyDescriptor(AfterThrowingAdviceDeclaration.class, "pointcut", PointcutDesignator.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$
		
	public static final ChildListPropertyDescriptor throwingTHROWN_EXCEPTIONS_PROPERTY = 
		new ChildListPropertyDescriptor(AfterThrowingAdviceDeclaration.class, "thrownExceptions", Name.class, NO_CYCLE_RISK); //$NON-NLS-1$
	
	public static final ChildPropertyDescriptor throwingBODY_PROPERTY = 
		new ChildPropertyDescriptor(AfterThrowingAdviceDeclaration.class, "body", Block.class, OPTIONAL, CYCLE_RISK); //$NON-NLS-1$

	protected static List throwingPROPERTY_DESCRIPTORS_2_0;
	
	protected static List throwingPROPERTY_DESCRIPTORS_3_0;
	
	public static final ChildPropertyDescriptor throwingTHROWING_PROPERTY = 
		new ChildPropertyDescriptor(AfterThrowingAdviceDeclaration.class, "throwing", SingleVariableDeclaration.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$
	
	public static List propertyDescriptors(int apiLevel) {
		if (apiLevel == AST.JLS2_INTERNAL) {
			return throwingPROPERTY_DESCRIPTORS_2_0;
		} else {
			return throwingPROPERTY_DESCRIPTORS_3_0;
		}
	}
	
	static {
		List propertyList = new ArrayList(6);
		createPropertyList(AfterThrowingAdviceDeclaration.class, propertyList);
		addProperty(throwingJAVADOC_PROPERTY, propertyList);
		addProperty(throwingPARAMETERS_PROPERTY, propertyList);
		addProperty(throwingTHROWN_EXCEPTIONS_PROPERTY, propertyList);
		addProperty(throwingPOINTCUT_PROPERTY, propertyList);
		addProperty(throwingTHROWING_PROPERTY, propertyList);
		addProperty(throwingBODY_PROPERTY, propertyList);
		throwingPROPERTY_DESCRIPTORS_2_0 = reapPropertyList(propertyList);
		
		propertyList = new ArrayList(6);
		createPropertyList(AfterThrowingAdviceDeclaration.class, propertyList);
		addProperty(throwingJAVADOC_PROPERTY, propertyList);
		addProperty(throwingPARAMETERS_PROPERTY, propertyList);
		addProperty(throwingTHROWN_EXCEPTIONS_PROPERTY, propertyList);
		addProperty(throwingPOINTCUT_PROPERTY, propertyList);
		addProperty(throwingTHROWING_PROPERTY, propertyList);
		addProperty(throwingBODY_PROPERTY, propertyList);		
		throwingPROPERTY_DESCRIPTORS_3_0 = reapPropertyList(propertyList);
	}
	
	private SingleVariableDeclaration throwing = null;
	
	AfterThrowingAdviceDeclaration(AST ast) {
		super(ast);
	}
	
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == throwingTHROWING_PROPERTY) {
			if (get) {
				return getThrowing();
			} else {
				setThrowing((SingleVariableDeclaration) child);
				return null;
			}
		}
		return super.internalGetSetChildProperty(property, get, child);
	}
	
	public SingleVariableDeclaration getThrowing() {
		return throwing;
	}
	
	public void setThrowing(SingleVariableDeclaration throwing) {
		this.throwing = throwing;
	}
	
	ASTNode clone0(AST target) {
		AfterThrowingAdviceDeclaration result = new AfterThrowingAdviceDeclaration(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		result.parameters().addAll(
			ASTNode.copySubtrees(target, parameters()));
		result.setPointcut(getPointcut());
		result.setThrowing(throwing);
		result.thrownExceptions().addAll(
			ASTNode.copySubtrees(target, thrownExceptions()));
		result.setBody(
			(Block) ASTNode.copySubtree(target, getBody()));
		return result;
	}
	
	int treeSize() {
		return
			super.treeSize()
			+ (this.throwing == null ? 0 : this.throwing.treeSize());
	}
	
	void accept0(ASTVisitor visitor) {
		if (visitor instanceof AjASTVisitor) {
			boolean visitChildren = ((AjASTVisitor)visitor).visit(this);
			if (visitChildren) {
				// visit children in normal left to right reading order
				acceptChild(visitor, getJavadoc());
				acceptChildren(visitor, this.parameters);
				acceptChild(visitor, getPointcut());
				acceptChild(visitor, getThrowing());
				acceptChildren(visitor, this.thrownExceptions);
				acceptChild(visitor, getBody());
			}
			((AjASTVisitor)visitor).endVisit(this);
		}
	}
}