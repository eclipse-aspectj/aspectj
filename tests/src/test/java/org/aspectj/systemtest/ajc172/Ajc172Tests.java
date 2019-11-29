/*******************************************************************************
 * Copyright (c) 2012 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc172;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc172Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testUnsupportedShouldBeNormalError_pr391384() {
		runTest("unsupported should be normal error");
	}
	
	// if the test is failing because the classes won't run, remove the run blocks from the ajc172.xml entry and re-run to check signatures.
	public void testSignatures_pr394535() throws Exception {
		runTest("signatures");
		
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(),"Bug2$ClassA2"); // the working one
		String sss = jc.getSignatureAttribute().getSignature();
		assertEquals("<T::LBug2$Interface12;:LBug2$Interface22;>Ljava/lang/Object;Ljava/io/Serializable;", sss);
		
		jc = getClassFrom(ajc.getSandboxDirectory(),"Bug$ClassA");
		sss = jc.getSignatureAttribute().getSignature();
		assertEquals("<T::LBug$Interface1;:LBug$Interface2;>Ljava/lang/Object;Ljava/io/Serializable;", sss);
	}
	
	// extends
	public void testPSignatures_pr399590() throws Exception {
		runTest("p signatures 1");
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(),"Cage");
		String sss = jc.getSignatureAttribute().getSignature();
		assertEquals("<T:LAnimal<+LCage<TT;>;>;>LBar;", sss);
		jc = getClassFrom(ajc.getSandboxDirectory(),"Cage2");
		sss = jc.getSignatureAttribute().getSignature();
		assertEquals("<T:LAnimal2<+LCage2<TT;>;>;>LBar2;Ljava/io/Serializable;", sss);
	}
	
	// extends two classes
	public void testPSignatures_pr399590_2() throws Exception {
		runTest("p signatures 2");
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(),"Cage");
		String sss = jc.getSignatureAttribute().getSignature();
		assertEquals("<T:LAnimal<+LCage<TT;LIntf;>;LIntf;>;Q:Ljava/lang/Object;>LBar;", sss);
		jc = getClassFrom(ajc.getSandboxDirectory(),"Cage2");
		sss = jc.getSignatureAttribute().getSignature();
		assertEquals("<T:LAnimal2<+LCage2<TT;LIntf2;>;LIntf2;>;Q:Ljava/lang/Object;>LBar2;Ljava/io/Serializable;", sss);
	}
	
	// super
	public void testPSignatures_pr399590_3() throws Exception {
		runTest("p signatures 3");
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(),"Cage");
		String sss = jc.getSignatureAttribute().getSignature();
		assertEquals("<T:LAnimal<-LXXX<TT;>;>;>LBar;", sss);
		jc = getClassFrom(ajc.getSandboxDirectory(),"Cage2");
		sss = jc.getSignatureAttribute().getSignature();
		assertEquals("<T:LAnimal2<-LXXX2<TT;>;>;>LBar2;Ljava/io/Serializable;", sss);
	}

	// super
	public void testPSignatures_pr399590_4() throws Exception {
		runTest("p signatures 4");
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(),"Cage");
		String sss = jc.getSignatureAttribute().getSignature();
		assertEquals("<T:LAnimal<-LXXX<TT;>;LYYY;>;>LBar;", sss);
		jc = getClassFrom(ajc.getSandboxDirectory(),"Cage2");
		sss = jc.getSignatureAttribute().getSignature();
		assertEquals("<T:LAnimal2<-LXXX2<TT;>;LYYY2;>;>LBar2;Ljava/io/Serializable;", sss);
	}

	// unbound
	public void testPSignatures_pr399590_5() throws Exception {
		runTest("p signatures 5");
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(),"Cage");
		String sss = jc.getSignatureAttribute().getSignature();
		assertEquals("<T:LAnimal<*>;>LBar;", sss);
		jc = getClassFrom(ajc.getSandboxDirectory(),"Cage2");
		sss = jc.getSignatureAttribute().getSignature();
		assertEquals("<T:LAnimal2<*>;>LBar2;Ljava/io/Serializable;", sss);
	}

	public void testIfPointcutNames_pr398246() throws Exception {
		runTest("if pointcut names");
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), "X");
		Method m = getMethodStartsWith(jc, "ajc$if");
		assertEquals("ajc$if$andy", m.getName());
	}

	public void testIfPointcutNames_pr398246_2() throws Exception {
		runTest("if pointcut names 2");
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), "X");
		Method m = getMethodStartsWith(jc, "ajc$if");
		assertEquals("ajc$if$fred", m.getName());
	}

	// fully qualified annotation name is used
	public void testIfPointcutNames_pr398246_3() throws Exception {
		runTest("if pointcut names 3");
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), "X");
		Method m = getMethodStartsWith(jc, "ajc$if");
		assertEquals("ajc$if$barney", m.getName());
	}

	// compiling a class later than the initial build - does it pick up the
	// right if clause name?
	public void testIfPointcutNames_pr398246_4() throws Exception {
		runTest("if pointcut names 4");
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), "X");
		Method m = getMethodStartsWith(jc, "ajc$if");
		assertEquals("ajc$if$sid", m.getName());
	}

	// new style generated names
	public void testIfPointcutNames_pr398246_5() throws Exception {
		runTest("if pointcut names 5");
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), "X");
		Method m = getMethodStartsWith(jc, "ajc$if");
		assertEquals("ajc$if$ac0cb804", m.getName());

		jc = getClassFrom(ajc.getSandboxDirectory(), "X2");
		m = getMethodStartsWith(jc, "ajc$if");
		assertEquals("ajc$if$ac0cb804", m.getName());
	}

	// new style generated names - multiple ifs in one pointcut
	public void testIfPointcutNames_pr398246_6() throws Exception {
		runTest("if pointcut names 6");
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), "X");
		Method m = getMethodStartsWith(jc, "ajc$if",1);
		assertEquals("ajc$if$aac93da8", m.getName());
		m = getMethodStartsWith(jc, "ajc$if",2);
		assertEquals("ajc$if$1$ae5e778a", m.getName());
	}

	// new style generated names - multiple ifs in one advice
	public void testIfPointcutNames_pr398246_7() throws Exception {
		runTest("if pointcut names 7");
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), "X");
		Method m = getMethodStartsWith(jc, "ajc$if",1);
		assertEquals("ajc$if$1$ac0607c", m.getName());
		m = getMethodStartsWith(jc, "ajc$if",2);
		assertEquals("ajc$if$1$1$4d4baf36", m.getName());
	}

	public void testOptionalAspects_pr398588() {
		runTest("optional aspects");
	}

	public void testInconsistentClassFile_pr389750() {
		runTest("inconsistent class file");
	}

	public void testInconsistentClassFile_pr389750_2() {
		runTest("inconsistent class file 2");
	}

	public void testInconsistentClassFile_pr389750_3() {
		runTest("inconsistent class file 3");
	}

	public void testInconsistentClassFile_pr389750_4() {
		runTest("inconsistent class file 4");
	}

	public void testAnnotationValueError_pr389752_1() {
		runTest("annotation value error 1");
	}

	public void testAnnotationValueError_pr389752_2() {
		runTest("annotation value error 2");
	}

	// this needs some cleverness to fix... the annotation value is parsed as a
	// string and then not checked
	// to see if the user is accidentally supplying, for example, an enum value.
	// Due to the use of strings, it
	// is hard to check. The verification code might go here:
	// WildAnnotationTypePattern, line 205 (the string case)
	// public void testAnnotationValueError_pr389752_3() {
	// runTest("annotation value error 3");
	// }

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc172Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc172.xml");
	}

}
