/********************************************************************
 * Copyright (c) 2005 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 *******************************************************************/
package org.aspectj.tools.ajc;

import java.lang.reflect.Modifier;
import java.util.HashMap;

import junit.framework.TestCase;

import org.aspectj.org.eclipse.jdt.core.dom.AST;
import org.aspectj.org.eclipse.jdt.core.dom.ASTParser;
import org.aspectj.org.eclipse.jdt.core.dom.AjASTVisitor;
import org.aspectj.org.eclipse.jdt.core.dom.CompilationUnit;
import org.aspectj.org.eclipse.jdt.core.dom.InterTypeFieldDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.InterTypeMethodDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.MethodDeclaration;

public class ASTitdTest extends TestCase {

	public void testAspectWithPublicMethodITD() {
		checkNameAndModifiers("aspect A{ public void B.x(){} }","name = x, modifier = public");
	}

	public void testAspectWithPrivateMethodITD() {
		checkNameAndModifiers("aspect A{ private void B.x(){} }","name = x, modifier = private");
	}

	public void testAspectWithPublicAbstractMethodITD() {
		checkNameAndModifiers("aspect A{ public abstract void B.x(){} }","name = x, modifier = public abstract");
	}

	public void testAspectWithConstructorITD() {
		checkNameAndModifiers("class A {}aspect B {public A.new(){}}","name = A_new, modifier = public");
	}
	
	public void testAspectWithPublicFieldITD() {
		checkNameAndModifiers("class A {}aspect B {public int A.a;}","name = a, modifier = public");
	}
	
	private void checkNameAndModifiers(String source, String expectedOutput){
		ASTParser parser = ASTParser.newParser(AST.JLS2); // ajh02: need to use 2 for returnType - in 3 it has "returnType2"
		parser.setCompilerOptions(new HashMap());//JavaCore.getOptions());
		parser.setSource(source.toCharArray());
		CompilationUnit cu2 = (CompilationUnit) parser.createAST(null);
		ITDTestVisitor visitor = new ITDTestVisitor();
		cu2.accept(visitor);
		String result = visitor.toString();
		//System.err.println("actual:\n" + result);
		assertTrue("Expected:\n"+ expectedOutput + "====Actual:\n" + result,
				expectedOutput.equals(result));
	}
	
}

class ITDTestVisitor extends AjASTVisitor {

	StringBuffer b = new StringBuffer();
	boolean visitDocTags;
	
	ITDTestVisitor() {
		this(false);
	}
	
	public String toString(){
		return b.toString();
	}
	
	ITDTestVisitor(boolean visitDocTags) {
		super(visitDocTags);
		this.visitDocTags = visitDocTags;
	}
	
	public boolean visit(MethodDeclaration node) {
		if (node instanceof InterTypeMethodDeclaration) 
			return visit((InterTypeMethodDeclaration)node);
		return true;
	}
	public boolean visit(InterTypeFieldDeclaration node) {
		b.append("name = " + node.fragments().get(0) + ", modifier = " + Modifier.toString(node.getModifiers()));
		return true;
	}
	public boolean visit(InterTypeMethodDeclaration node) {
		b.append("name = " + node.getName() + ", modifier = " + Modifier.toString(node.getModifiers()));
		return true;
	}	
}
