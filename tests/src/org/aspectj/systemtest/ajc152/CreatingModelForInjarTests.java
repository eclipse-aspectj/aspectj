/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version
 *******************************************************************/
package org.aspectj.systemtest.ajc152;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.testing.XMLBasedAjcTestCase;
//import org.aspectj.weaver.World;

public class CreatingModelForInjarTests extends org.aspectj.testing.XMLBasedAjcTestCase {
	
	public void testAdviceAndNamedPCD() {
		runTest("advice and deow");

		// expect:
		// 		- pkg {package}
		// 			-  A.aj (binary) {java source file}
		//				- import declarations {import reference}
		//				- A {aspect}
		//					- p {pointcut}
		//					- before {advice}

		IProgramElement pkgNode = getPkgNode();
		IProgramElement srcFile = checkChild(pkgNode,IProgramElement.Kind.FILE_JAVA,"A.aj (binary)",1);
		checkChild(srcFile,IProgramElement.Kind.IMPORT_REFERENCE,"import declarations",-1);
		IProgramElement aspectNode = checkChild(srcFile,IProgramElement.Kind.ASPECT,"A",-1);
		checkChild(aspectNode,IProgramElement.Kind.POINTCUT,"p",5);
		checkChild(aspectNode,IProgramElement.Kind.ADVICE,"before",7);	
	}

	public void testDeclareWarning() {
		runTest("advice and deow");		

		// expect:
		// 		- pkg {package}
		// 			-  Deow.aj (binary) {java source file}
		//				- import declarations {import reference}
		//				- Deow {aspect}
		//					- declare warning {declare warning}

		IHierarchy top = AsmManager.getDefault().getHierarchy();
		IProgramElement dwNode = top.findElementForLabel(top.getRoot(),
				IProgramElement.Kind.DECLARE_WARNING,
				"declare warning: \"There should be n..\"");
		assertNotNull("Couldn't find 'declare warning: \"There should be n..\"' " +
				"element in the tree",dwNode);
		assertEquals("expected 'declare warning: \"There should be n..\"'" + 
				" to be on line 5 but was on " + dwNode.getSourceLocation().getLine(),
				5, dwNode.getSourceLocation().getLine());			
	}
	
	public void testNumberOfPackageNodes() {
		runTest("advice and deow");
		// check that the 'pkg' package node has not been added twice
		IProgramElement root = AsmManager.getDefault().getHierarchy().getRoot();
		List l = root.getChildren();
		int numberOfPkgs = 0;
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			IProgramElement element = (IProgramElement) iter.next();
			if (element.getKind().equals(IProgramElement.Kind.PACKAGE)
					&& element.getName().equals("pkg")) {
				numberOfPkgs++;
			}
		}
		assertEquals("expected one package called 'pkg' but found " + numberOfPkgs,1,numberOfPkgs);		
	}
	
	private IProgramElement getPkgNode() {
		IHierarchy top = AsmManager.getDefault().getHierarchy();
		IProgramElement pkgNode = top.findElementForLabel(top.getRoot(),
				IProgramElement.Kind.PACKAGE,"pkg");
		assertNotNull("Couldn't find 'pkg' element in the tree",pkgNode);
		return pkgNode;
	}
	
	private IProgramElement checkChild(IProgramElement parent,
			IProgramElement.Kind childKind,
			String childName,
			int childLineNumbr) {
		List children = parent.getChildren();
		boolean foundChild = false;
		for (Iterator iter = children.iterator(); iter.hasNext();) {
			IProgramElement element = (IProgramElement) iter.next();
			if (element.getKind().equals(childKind) 
					&& element.getName().equals(childName) ) {
				foundChild = true;
				if (childLineNumbr != -1) {
					assertEquals("expected " + childKind.toString() + childName + 
							" to be on line " + childLineNumbr + " but was on " + 
							element.getSourceLocation().getLine(),
							childLineNumbr, element.getSourceLocation().getLine());			
				}
				return element;
			}
		}	
		assertTrue("expected " + parent.getName() + " to have child " + childName 
				+ " but it did not", foundChild);
		return null;
	}
	
	protected void setUp() throws Exception {
		super.setUp();
//		World.createInjarHierarchy = true;
	}

	protected void tearDown() throws Exception {
		super.tearDown();
//        World.createInjarHierarchy = false;
	}

	// ///////////////////////////////////////
	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(CreatingModelForInjarTests.class);
	}

	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc152/injar.xml");
	}
}
