/*******************************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.aspectj.org.eclipse.jdt.core.dom;

import java.util.List;

import junit.framework.TestCase;

/**
 * The "modifiers" property descriptors of the declare nodes must name the node class they belong to, not that of another
 * declare node. This test is in the DOM package because the descriptors are only available package-privately.
 */
public class DeclareModifiersPropertyTest extends TestCase {

	@SuppressWarnings("deprecation")
	public void testModifiersPropertiesBelongToTheirOwnNodeClass() {
		AjAST ast = AjAST.newAjAST(AST.getJLSLatest(), false);
		List<BodyDeclaration> declarations = List.of(
				ast.newDeclareAtConstructorDeclaration(), ast.newDeclareAtFieldDeclaration(),
				ast.newDeclareAtMethodDeclaration(), ast.newDeclareAtTypeDeclaration(),
				ast.newDeclareErrorDeclaration(), ast.newDeclareParentsDeclaration(),
				ast.newDeclarePrecedenceDeclaration(), ast.newDeclareSoftDeclaration(),
				ast.newDeclareWarningDeclaration());
		for (BodyDeclaration declaration : declarations) {
			Class<?> nodeClass = declaration.getClass();
			assertEquals(nodeClass, declaration.internalModifiersProperty().getNodeClass());
			assertEquals(nodeClass, declaration.internalModifiers2Property().getNodeClass());
		}
	}
}
