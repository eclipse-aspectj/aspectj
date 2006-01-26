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

import org.aspectj.org.eclipse.jdt.core.dom.ASTParser.IASTFactory;

public class AjASTFactory implements IASTFactory {

	public AST getAST(int level) {
		return AjAST.newAjAST(level);
	}

}
