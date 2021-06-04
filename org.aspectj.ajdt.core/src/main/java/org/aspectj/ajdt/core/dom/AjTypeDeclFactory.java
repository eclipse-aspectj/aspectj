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
package org.aspectj.ajdt.core.dom;

import org.aspectj.org.eclipse.jdt.core.dom.AST;
import org.aspectj.org.eclipse.jdt.core.dom.AjTypeDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.TypeDeclaration.ITypeDeclFactory;

/**
 * Factory, dynamically loaded by the TypeDeclaration class in the shadows/dom tree.
 * This is a factory for type declaration that returns the Aj subclass of typedeclaration.
 * @author AndyClement
 */
public class AjTypeDeclFactory implements ITypeDeclFactory {
	public TypeDeclaration createTypeFor(AST ast) {
		return new AjTypeDeclaration(ast);
	}
}
