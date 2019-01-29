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

import org.aspectj.org.eclipse.jdt.core.dom.AST;
import org.aspectj.org.eclipse.jdt.core.dom.ASTNode;

/**
 * ReferencePointcut DOM AST node.
 * has:
 *   a name
 *   
 * note:
 *   should also have a parameter list like method invokations do?
 * @author ajh02
 */
public class ReferencePointcut extends PointcutDesignator {	
	
	private SimpleName pointcutName = null;
	public static final ChildPropertyDescriptor NAME_PROPERTY = 
		new ChildPropertyDescriptor(ReferencePointcut.class, "name", SimpleName.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$
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
	
	
	
	
	ReferencePointcut(AST ast) {
		super(ast);
	}
	public static List propertyDescriptors(int apiLevel) {
		List propertyList = new ArrayList(1);
		createPropertyList(ReferencePointcut.class, propertyList);
		addProperty(NAME_PROPERTY, propertyList);
		return reapPropertyList(propertyList);
	}
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == NAME_PROPERTY) {
			if (get) {
				return getName();
			} else {
				setName((SimpleName) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}
	ASTNode clone0(AST target) {
		ReferencePointcut result = new ReferencePointcut(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setName((SimpleName) getName().clone(target));
		return result;
	}
	final boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return ((AjASTMatcher)matcher).match(this, other);
	}
	void accept0(ASTVisitor visitor) {
		if (visitor instanceof AjASTVisitor) {
			boolean visitChildren = ((AjASTVisitor)visitor).visit(this);
			if (visitChildren) {
				// visit children in normal left to right reading order
				acceptChild(visitor, getName());
				// todo: accept the parameters here
			}
			((AjASTVisitor)visitor).endVisit(this);
		}
	}
	int treeSize() {
		return
			memSize()
			+ (this.pointcutName == null ? 0 : getName().treeSize());
	}
}