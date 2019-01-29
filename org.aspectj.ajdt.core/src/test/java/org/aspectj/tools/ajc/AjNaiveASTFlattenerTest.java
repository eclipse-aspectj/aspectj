/********************************************************************
 * Copyright (c) 2006, 2010 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Eduardo Piveta  - initial version
 *               Helen Hawkins   - ammended to fit within JUnit framework
 *******************************************************************/
package org.aspectj.tools.ajc;

import java.util.HashMap;

import junit.framework.TestCase;

import org.aspectj.org.eclipse.jdt.core.dom.AST;
import org.aspectj.org.eclipse.jdt.core.dom.ASTParser;
import org.aspectj.org.eclipse.jdt.core.dom.AjNaiveASTFlattener;
import org.aspectj.org.eclipse.jdt.core.dom.CompilationUnit;

public class AjNaiveASTFlattenerTest extends TestCase {

	public void testNoPointcutArgs() {
		check("public aspect A { pointcut y(): call(* *.*(..));}",
				"public aspect A {\n   pointcut y():call(* *.*(..));\n}\n");
	}

	public void testOneIntPointcutArg() {
		check("public aspect A { pointcut y(int a): call(* *.*(..));}",
				"public aspect A {\n   pointcut y(int a):call(* *.*(..));\n}\n");		
	}
	
	public void testOneIntOneDoublePointcutArg() {
		check("public aspect A { pointcut y(int a, double b): call(* *.*(..));}",
				"public aspect A {\n   pointcut y(int a, double b):call(* *.*(..));\n}\n");		
	}
	
	public void testOneTypedPointcutArg() {
		check("public aspect A { pointcut y(X a): call(* *.*(..));}",
				"public aspect A {\n   pointcut y(X a):call(* *.*(..));\n}\n");		
	}
	
	public void testTwoTypedPointcutArgs() {
		check("public aspect A { pointcut y(X a, X b): call(* *.*(..));}",
				"public aspect A {\n   pointcut y(X a, X b):call(* *.*(..));\n}\n");		
	}
	
	public void testOneTypedAndOneIntPointcutArg() {
		check("public aspect A { pointcut y(X a, int b): call(* *.*(..));}",
				"public aspect A {\n   pointcut y(X a, int b):call(* *.*(..));\n}\n");		
	}
	
	public void testOneIntAndOneTypedPointcutArg() {
		check("public aspect A { pointcut y(int a, X b): call(* *.*(..));}",
				"public aspect A {\n   pointcut y(int a, X b):call(* *.*(..));\n}\n");		
	}
	
	public void testOneIntOneDoubleAndOneTypedPointcutArg() {
		check("public aspect A { pointcut y(int a, double b, Y c): call(* *.*(..));}",
				"public aspect A {\n   pointcut y(int a, double b, Y c):call(* *.*(..));\n}\n");
	}
	
	public void testDeclareParentsDeclaration() throws Exception {
		check("public aspect A { declare parents: X extends Y; }",
				"public aspect A {\n  declare parents: X extends Y;\n}\n");
	}
	
	
	/*
	 * 
	 * 
	 * START: Test TypePattern nodes introduced in Bugzilla 329268.
	 * 
	 * 
	 */
	
	public void testDeclareParentsDeclarationAny() throws Exception {
		check("public aspect A { declare parents: * extends Y; }",
				"public aspect A {\n  declare parents: * extends Y;\n}\n");
	}
	
	public void testDeclareParentsAndDeclaration() throws Exception {
		check("public aspect A { declare parents: W && X && Y extends Z; }",
				"public aspect A {\n  declare parents: W && X && Y extends Z;\n}\n");
	}
	
	public void testDeclareParentsOrDeclaration() throws Exception {
		check("public aspect A { declare parents: W || X || Y extends Z; }",
				"public aspect A {\n  declare parents: W || X || Y extends Z;\n}\n");
	}
	
	public void testDeclareParentsNot() throws Exception {
		check("public aspect A { declare parents: W && !X extends Z; }",
				"public aspect A {\n  declare parents: W && !X extends Z;\n}\n");
	}
	
	public void testDeclareParentsTypeCategory() throws Exception {
		check("public aspect A { declare parents: B && is(AnonymousType) extends Z; }",
		"public aspect A {\n  declare parents: B && is(AnonymousType) extends Z;\n}\n");
		
	}
	
	public void testDeclareParentsTypeCategoryNot() throws Exception {
		check("public aspect A { declare parents: B && !is(InnerType) extends Z; }",
		"public aspect A {\n  declare parents: B && !is(InnerType) extends Z;\n}\n");
	}

	
	// TODO: commented until hasmethod is supported in AspectJ
//	public void testDeclareParentsHasMember() {
//		check("public aspect A { declare parents : A && hasmethod(void foo*(..)) extends D; }",
//		"public aspect A {\n  declare parents : A && hasmethod(void foo*(..)) extends D;\n}\n");
//	}

	
	/*
	 * 
	 * 
	 * END: Test TypePattern nodes introduced in Bugzilla 329268.
	 * 
	 * 
	 */
	
	public void testDeclareWarning() throws Exception {
		check("public aspect A { declare warning: call(* *.*(..)) : \"warning!\"; }",
				"public aspect A {\n  declare warning: call(* *.*(..)) : \"warning!\" ;\n}\n");
	}
	
	public void testDeclareErrorDeclaration() throws Exception {
		check("public aspect A { declare error: call(* *.*(..)) : \"error!\"; }",
		"public aspect A {\n  declare error: call(* *.*(..)) : \"error!\" ;\n}\n");
	}
	
	public void testDeclareSoftDeclaration() throws Exception {
		check("public aspect A { declare soft: X : call(* *.*(..)); }",
		"public aspect A {\n  declare soft: X : call(* *.*(..)) ;\n}\n");
	}
	
	public void testDeclarePrecedenceDeclaration() throws Exception {
		check("public aspect A { declare precedence: X, Y, Z; }",
				"public aspect A {\n  declare precedence: X, Y, Z;\n}\n");
	}

	private void check(String source, String expectedOutput) {
		ASTParser parser = ASTParser.newParser(AST.JLS2);
		parser.setCompilerOptions(new HashMap());
		parser.setSource(source.toCharArray());
		CompilationUnit cu2 = (CompilationUnit) parser.createAST(null);
		AjNaiveASTFlattener visitor = new AjNaiveASTFlattener();
		cu2.accept(visitor);
		String result = visitor.getResult();
		System.err.println(result);
		assertTrue("Expected:\n"+ expectedOutput + "====Actual:\n" + result,
				expectedOutput.equals(result));
	}

}
