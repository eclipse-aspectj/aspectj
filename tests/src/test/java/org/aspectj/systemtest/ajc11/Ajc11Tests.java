/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * ******************************************************************/
package org.aspectj.systemtest.ajc11;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class Ajc11Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc11Tests.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("ajc11.xml");
	}

	public void test001() {
		runTest("declare interface extends class");
	}

	public void test002() {
		runTest("declare interface implements class");
	}

	public void test003() {
		runTest("declaring a private method on an inner interface");
	}

	public void test004() {
		runTest("CE expected when declaring fields on arrays");
	}

	public void test005() {
		runTest("signature of handler join point");
	}

	public void test006() {
		runTest("source locations within expressions");
	}

	public void test007() {
		runTest("crashes given method in declared method");
	}

	public void test008() {
		runTest("after returning advice on interface constructor");
	}

	public void test009() {
		runTest("after returning advice on interface constructor - error");
	}

	public void test010() {
		runTest("after advice on static call join point");
	}

	public void test011() {
		runTest("incompatible class change error");
	}

	public void test012() {
		runTest("simple cflow of method execution");
	}

	public void test013() {
		runTest("using instance as class reference to constant field");
	}

	public void test014() {
		runTest("interface self-reference in anonymous instance");
	}

	public void test015() {
		runTest("self-reference from (aspect-declared) method-local class");
	}

	public void test016() {
		runTest("expect CE for unterminated declare error");
	}

	public void test017() {
		runTest("expect CE for declaration collision between subaspects instead of domination order");
	}

	public void test018() {
		runTest("subtype pattern in dominates should pick out aspect subtypes");
	}

	public void test019() {
		runTest("subtype pattern in dominates will conflict with type pattern");
	}

	public void test020() {
		runTest("after returning advice on interface and implementation constructor");
	}

	public void test021() {
		runTest("after throwing advice with non-throwable formal");
	}

	public void test022() {
		runTest("declare array field using postfix");
	}

	public void test023() {
		runTest("prohibit declaring new aspect constructor with arguments");
	}

	public void test024() {
		runTest("prohibit declaring only aspect constructor with arguments");
	}

	public void test025() {
		runTest("declare class extends interface");
	}

	public void test026() {
		runTest("declare class implements class");
	}

	public void test027() {
		runTest("declare interface implements interface");
	}

	public void test028() {
		runTest("if and cflow arg binding");
	}

	public void test029() {
		runTest("circularity in declare dominates");
	}

	public void test030() {
		runTest("percflow code hangs compiler");
	}

	public void test031() {
		runTest("Verification error tracing constructor that takes arguments");
	}

	public void test032() {
		runTest("declared exceptions in inter-type decls");
	}

	public void test033() {
		runTest("Verify error on non-Throwable in declare soft");
	}

	public void test034() {
		runTest("inter-type fields with array types");
	}

	public void test035() {
		runTest("checking around join point for advice return type - numeric");
	}

	public void test036() {
		runTest("void around advice without proceed");
	}

	public void test037() {
		runTest("declaring method on superclass and subclass");
	}

	public void test038() {
		runTest("introducing final fields (simple)");
	}

	public void test039() {
		runTest("introducing final fields and using as constants");
	}

	public void test040() {
		runTest("introducing final fields and checking errors");
	}

	public void test041() {
		runTest("Static inner aspects cannot reference user defined pointcuts");
	}

	public void test042() {
		runTest("Static inner aspects cannot reference user defined pointcuts");
	}

	public void test043() {
		runTest("Declare precedence should not allow multiple * patterns");
	}

	public void test044() {
		runTest("VerifyError on accessing objects not accessible to the weaver");
	}

	public void test045() {
		runTest("aspect static initializers should run before instance constructed");
	}

	public void test046() {
		runTest("super call in intertype method declaration body causes VerifyError");
	}

	public void test047() {
		runTest("Error with certain combination of advice");
	}

	public void test048() {
		runTest("Pointcut adviceexecution() does not work");
	}

	public void test049() {
		runTest("problems with finalize call");
	}

	public void test050() {
		runTest("Negation of if pointcut does not work");
	}

	public void test051() {
		runTest("ajc reports error when encountering static declaration of nested classes");
	}

	public void test052() {
		runTest("can't use pointcuts defined in inner aspects ");
	}

	public void test053() {
		runTest("can't resolve nested public interfaces (also PR#32399)");
	}

	public void test054() {
		runTest("thisJoinPoint.getArgs() causes IncompatibleClassChangeError");
	}

	public void test055() {
		runTest("inter-type declaration of void field");
	}

	public void test056() {
		runTest("no such constructor for proceed argument (error)");
	}

	public void test057() {
		runTest("omnibus declare warning context with no initializer/constructor");
	}

	public void test058() {
		runTest("omnibus declare warning context");
	}

	public void test059() {
		runTest("cflow binding issues with ignoring state");
	}

	public void test060() {
		runTest("cflow binding -- original weaver crash");
	}

	public void test061() {
		runTest("type not imported in around advice");
	}

	public void test062() {
		runTest("type not imported in aspect");
	}

	public void test063() {
		runTest("class extending abstract aspect");
	}

	public void test064() {
		runTest("declare soft and throw statements");
	}

	public void test065() {
		runTest("inter-type declaration bug with abstract classes");
	}

	public void test066() {
		runTest("Inter type declaration to base class not seen by derived class");
	}

	public void test067() {
		runTest("Declare parents with intermediate ancestor");
	}

	public void test068() {
		runTest("Declare parents removing ancestor");
	}

	public void test069() {
		runTest("IllegalAccessError while accessing introduced variable / 1.1rc1");
	}

	public void test070() {
		runTest("implemented abstract pointcut");
	}

	public void test071() {
		runTest("privileged aspect main verify error");
	}

	public void test072() {
		runTest("Internal compiler error with thisJoinPoint.getStaticPart()");
	}

	public void test073() {
		runTest("Inconsistant stack height with around");
	}

	public void test074() {
		runTest("Ajc 1.1 rc1 java.lang.VerifyError with messy arounds");
	}

	public void test075() {
		runTest("try/finally in around advice (same as ...messy arounds?)");
	}

	public void test076() {
		runTest("advise join points in subclass of empty interface");
	}

	public void test077() {
		runTest("can't put around advice on interface static initializer");
	}

	public void test078() {
		runTest("cflow concretization causing assertion failure");
	}

	public void test079() {
		runTest("lame error message: negation doesn't allow binding");
	}

	public void test080() {
		runTest("Error when introducing members of type Class");
	}

	// public void test081(){
	// runTest("arrays via Class.forName()");
	// }

	public void test082() {
		runTest("perthis and inline arounds");
	}

	public void test083() {
		runTest("Weaver fails with NPE for very large source files ");
	}

	public void test084() {
		runTest("CLE: no sources");
	}

	public void test085() {
		runTest("CLE: bad filename");
	}

	public void test086() {
		runTest("CLE: no dir specified for sourceroots");
	}

	public void test087() {
		runTest("CLE: no sourceroot specified for incremental");
	}

	public void test088() {
		runTest("CLE: file specified with incremental");
	}

	public void test089() {
		runTest("public static fields being ignored");
	}

	public void test090() {
		runTest("can not resolve this member warning");
	}

	public void test091() {
		runTest("try switch VerifyError, InconsistentStackHeight");
	}

	public void test092() {
		runTest("Compiler crash in ajc 1.1 - terrible error for inaccessible constructor - 1.7");
	}

	public void test093() {
		runTest("Missing import crashes compiler");
	}

	public void test094() {
		runTest("NPE in bcel.LazyMethodGen when delegating from one ctor to a second that includes a switch");
	}

	public void test095() {
		runTest("switch statement in aspects crashes weaving");
	}

	public void test096() {
		runTest("ajc stack trace on declaring hashcode() method in aspect");
	}

	public void test097() {
		runTest("using super in method introduced on interface with multiple supertypes");
	}

	public void test098() {
		runTest("Compiler crashes in jar and cflow (with no .jar)");
	}

	public void test099() {
		runTest("Compiler crashes in jar and cflow (with .jar)");
	}

	public void test100() {
		runTest("Default method impl for interface causes internal exception.");
	}

	public void test102() {
		runTest("compile error expected for abstract pointcut outside abstract aspect");
	}

	public void test103() {
		runTest("subtype-qualified pointcut reference");
	}

	public void test104() {
		runTest("weaver trace on mis-qualified pointcut reference");
	}

	public void test105() {
		runTest("compile error expected for interface pointcuts");
	}

	public void test106() {
		runTest("interface call signatures when declaring method in aspect");
	}

	public void test107() {
		runTest("reflective check of declared exceptions from aspect-declared methods");
	}

	public void test108() {
		runTest("throw derivative pointcuts not advised");
	}

	public void test109() {
		runTest("perthis and signature bad interaction");
	}

	public void test110() {
		runTest("declare error fails on pointcuts composed from multiple classes");
	}

	public void test111() {
		runTest("declare error fails on pointcuts composed from multiple classes");
	}

	public void test112() {
		runTest("Interaction between pointcut binding and declare parents");
	}

	public void test113() {
		runTest("Non-functional concretezation of ReferencePointcut");
	}

	public void test114() {
		runTest("zip and jar suffixes for extdirs entries");
	}

}
