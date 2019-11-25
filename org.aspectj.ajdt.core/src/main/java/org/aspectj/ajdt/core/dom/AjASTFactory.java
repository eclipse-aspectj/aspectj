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
package org.aspectj.ajdt.core.dom;

import org.aspectj.org.eclipse.jdt.core.dom.AST;
import org.aspectj.org.eclipse.jdt.core.dom.ASTParser.IASTFactory;
import org.aspectj.org.eclipse.jdt.core.dom.AjAST;

public class AjASTFactory implements IASTFactory {

	public AST getAST(int level, boolean previewEnabled) {
		return AjAST.newAjAST(level,previewEnabled);
	}

}
