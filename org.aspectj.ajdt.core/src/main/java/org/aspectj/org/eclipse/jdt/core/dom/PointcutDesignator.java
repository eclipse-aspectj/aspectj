/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.org.eclipse.jdt.core.dom;

import org.aspectj.org.eclipse.jdt.core.dom.AST;
import org.aspectj.org.eclipse.jdt.core.dom.ASTNode;

/**
 * abstract PointcutDesignator DOM AST node.
 * has:
 *   nothing at the moment
 *
 * @author ajh02
 */
public abstract class PointcutDesignator extends ASTNode {
	PointcutDesignator(AST ast) {
		super(ast);
	}
	final int getNodeType0() {
		return FIELD_DECLARATION; // ajh02: hmmmmmmm.. should make a POINTCUT_DESIGNATOR thing
	}
	int memSize() {
		return 0; // ajh02: stub method
	}
}
