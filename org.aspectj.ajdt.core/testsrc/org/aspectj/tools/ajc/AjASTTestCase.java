/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: Helen Hawkins   - initial implementation
 * 				 Matthew Webster - initial implementation
 *******************************************************************/
package org.aspectj.tools.ajc;

import java.util.HashMap;

import junit.framework.TestCase;

import org.aspectj.org.eclipse.jdt.core.dom.AST;
import org.aspectj.org.eclipse.jdt.core.dom.ASTParser;
import org.aspectj.org.eclipse.jdt.core.dom.AjAST;
import org.aspectj.org.eclipse.jdt.core.dom.CompilationUnit;

public abstract class AjASTTestCase extends TestCase {

	protected AjAST createAjAST() {
		return createAjAST(AST.JLS3);
	}

	protected AjAST createAjAST(int astlevel) {
		if (astlevel != AST.JLS2 && astlevel != AST.JLS3) {
			fail("need to pass AST.JLS2 or AST.JLS3 as an argument");
		}
		String source = "";
		ASTParser parser = ASTParser.newParser(astlevel);
		parser.setSource(source.toCharArray());
		parser.setCompilerOptions(new HashMap());
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		AST ast = cu.getAST();
		assertTrue("the ast should be an instance of AjAST",ast instanceof AjAST);
		return (AjAST)ast;
	}

	protected void checkJLS3(String source, int start, int length) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setCompilerOptions(new HashMap());
		parser.setSource(source.toCharArray());
		CompilationUnit cu2 = (CompilationUnit) parser.createAST(null);
		SourceRangeVisitor visitor = new SourceRangeVisitor();
		cu2.accept(visitor);
		int s = visitor.getStart();
		int l = visitor.getLength();
		assertTrue("Expected start position: "+ start + ", Actual:" + s,
				start == s);
		assertTrue("Expected length: "+ length + ", Actual:" + l,
				length == l);
	}

}
