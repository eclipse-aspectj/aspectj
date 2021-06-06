/********************************************************************
 * Copyright (c) 2006, 2010 Contributors. All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors: IBM Corporation - initial API and implementation
 * 				 Helen Hawkins   - iniital version
 *               Nieraj Singh
 *******************************************************************/
package org.aspectj.org.eclipse.jdt.core.dom;


/**
 * abstract TypePattern DOM AST node.
 * has:
 *   nothing at the moment
 */
public abstract class PatternNode extends ASTNode {

	PatternNode(AST ast) {
		super(ast);
	}
	int getNodeType0() {
		return FIELD_DECLARATION; // should make a PATTERN_NODE type constant
	}
	int memSize() {
		return 0; // stub method
	}

}
