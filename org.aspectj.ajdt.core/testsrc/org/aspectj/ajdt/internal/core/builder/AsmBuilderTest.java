/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.core.builder;

import java.util.EmptyStackException;

import junit.framework.*;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.LocalTypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public class AsmBuilderTest extends TestCase {

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
			
		};
		LocalTypeDeclaration local = new LocalTypeDeclaration(new CompilationResult(cu, 0, 0, 0));
		local.name = new char[2];
		BlockScope scope = null;
		
		try { 
			new AsmBuilder(new CompilationResult(cu, 0, 0, 0)).visit(local, scope);
		} catch (Exception e) {
			assertTrue(e instanceof EmptyStackException);
		}
	}

}  
