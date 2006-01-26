/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 *******************************************************************/
package org.aspectj.org.eclipse.jdt.core.dom;


/**
 * abstract TypePattern DOM AST node.
 * has:
 *   nothing at the moment
 */
public abstract class TypePattern extends PatternNode {

	TypePattern(AST ast) {
		super(ast);
	}

	int memSize() {
		return 0; // stub method
	}

}
