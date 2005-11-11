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
 * NotPointcut DOM AST node.
 * has:
 *   everything PointcutDesignators have
 *   a PointcutDesignator called 'body'
 *   
 * @author ajh02
 */

public class NotPointcut extends PointcutDesignator {	
	
	private PointcutDesignator body = null;
	public static final ChildPropertyDescriptor BODY_PROPERTY = 
		new ChildPropertyDescriptor(NotPointcut.class, "body", PointcutDesignator.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$
	public PointcutDesignator getBody() {
		return this.body;
	}
	public void setBody(PointcutDesignator body) {
		if (body == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.body;
		preReplaceChild(oldChild, body, BODY_PROPERTY);
		this.body = body;
		postReplaceChild(oldChild, body, BODY_PROPERTY);
	}
	
	
	
	
	NotPointcut(AST ast) {
		super(ast);
	}
	public static List propertyDescriptors(int apiLevel) {
		List propertyList = new ArrayList(1);
		createPropertyList(NotPointcut.class, propertyList);
		addProperty(BODY_PROPERTY, propertyList);
		return reapPropertyList(propertyList);
	}
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == BODY_PROPERTY) {
			if (get) {
				return getBody();
			} else {
				setBody((PointcutDesignator) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}
	ASTNode clone0(AST target) {
		NotPointcut result = new NotPointcut(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setBody((PointcutDesignator)body.clone0(target));
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
				acceptChild(visitor, getBody());
				// todo: accept the parameters here
			}
			((AjASTVisitor)visitor).endVisit(this);
		}
	}
	int treeSize() {
		return
			memSize()
			+ (this.body == null ? 0 : getBody().treeSize());
	}
}