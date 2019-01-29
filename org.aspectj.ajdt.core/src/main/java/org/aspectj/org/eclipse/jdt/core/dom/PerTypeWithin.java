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
 * OrPointcut DOM AST node.
 * has:
 *   everything PointcutDesignators have
 *   
 * note:
 *   should also have a typepattern or something. I haven't put one in yet.
 * @author ajh02
 */
public class PerTypeWithin extends PointcutDesignator {	
	
	PerTypeWithin(AST ast) {
		super(ast);
	}
	public static List propertyDescriptors(int apiLevel) {
		List propertyList = new ArrayList(1);
		createPropertyList(ReferencePointcut.class, propertyList);
		// add the type thingy here
		return reapPropertyList(propertyList);
	}
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		// ajh02: todo add type thingy here
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}
	ASTNode clone0(AST target) {
		PerTypeWithin result = new PerTypeWithin(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		// remeber to set the type thingy here
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
				// ajh02: remember to visit the type thingy here
			}
			((AjASTVisitor)visitor).endVisit(this);
		}
	}
	int treeSize() {
		return
			memSize(); // stub
	}
}