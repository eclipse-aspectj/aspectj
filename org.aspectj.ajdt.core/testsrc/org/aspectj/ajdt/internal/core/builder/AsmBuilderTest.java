/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.core.builder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public class AsmBuilderTest extends TestCase {

	private AsmHierarchyBuilder builder = new AsmHierarchyBuilder();
	
    public static Test suite() { 
        TestSuite suite = new TestSuite(AsmBuilderTest.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(AsmBuilderTest.class); 
        //$JUnit-END$
        return suite;
    }

	/**
	 * Test for bug#39626
	 */
	public void testNullHandlingOfVisit() { 
		ICompilationUnit cu = new ICompilationUnit() {
			public char[] getContents() {
				return null;
			}

			public char[] getMainTypeName() {
				return null;
			}

			public char[][] getPackageName() {
				return null;
			}
			
			public char[] getFileName() { 
				return null;
			}

			public boolean ignoreOptionalProblems() {
				return false;
			}
			
		};
		TypeDeclaration local = new TypeDeclaration(new CompilationResult(cu, 0, 0, 0));
		local.name = new char[2];
		BlockScope scope = null;
		
		try { 
//			builder.internalBuild(new CompilationResult(cu, 0, 0, 0), null);
			builder.visit(local, scope);
		} 
		catch (Exception e) {
			assertTrue(e instanceof NullPointerException);
		} 
// XXX put back?
//		catch (Exception e) {
//			assertTrue(e instanceof EmptyStackException);
//		}
	}

}  
