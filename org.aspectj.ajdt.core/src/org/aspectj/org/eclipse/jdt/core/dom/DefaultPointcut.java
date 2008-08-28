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
 * DefaultPointcut DOM AST node. has: nothing
 * 
 * This class is a stub and should be deleted when concrete subclasses exist for all the different types of pointcuts in AspectJ.
 * 
 * @author ajh02
 */

public class DefaultPointcut extends PointcutDesignator {
	private String detail;

	DefaultPointcut(AST ast, String d) {
		super(ast);
		this.detail = d;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String d) {
		this.detail = d;
	}

	public static List propertyDescriptors(int apiLevel) {
		List propertyList = new ArrayList(0);
		createPropertyList(DefaultPointcut.class, propertyList);
		return reapPropertyList(propertyList);
	}

	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}

	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	ASTNode clone0(AST target) {
		DefaultPointcut result = new DefaultPointcut(target, getDetail());
		result.setSourceRange(this.getStartPosition(), this.getLength());
		return result;
	}

	final boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return ((AjASTMatcher) matcher).match(this, other);
	}

	void accept0(ASTVisitor visitor) {
		if (visitor instanceof AjASTVisitor) {
			// boolean visitChildren =
			((AjASTVisitor) visitor).visit(this);
			((AjASTVisitor) visitor).endVisit(this);
		}
	}

	int treeSize() {
		return memSize();
	}
}