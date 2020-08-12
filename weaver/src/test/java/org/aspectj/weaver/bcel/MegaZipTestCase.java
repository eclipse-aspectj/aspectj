/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.weaver.bcel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.MemberImpl;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.WeaverTestCase;

public class MegaZipTestCase extends WeaveTestCase {

	private File outDir;

	public MegaZipTestCase(String arg0) {
		super(arg0);
	}

	public void setUp() throws Exception {
		super.setUp();
		outDir = WeaverTestCase.getOutdir();
	}

	public void tearDown() throws Exception {
		super.tearDown();
		WeaverTestCase.removeOutDir();
		outDir = null;
	}

	private BcelAdvice makeAroundMunger(final boolean matchOnlyPrintln) {
		// BcelWorld world = new BcelWorld();
		final Member sig = MemberImpl.method(UnresolvedType.forName("fluffy.Aspect"), Modifier.STATIC, "aroundFun",
				"(Lorg/aspectj/runtime/internal/AroundClosure;)Ljava/lang/Object;");

		return new BcelAdvice(AdviceKind.stringToKind("around"), matchOnlyPrintln ? makePointcutPrintln() : makePointcutAll(), sig,
				0, -1, -1, null, null) {
			public void specializeOn(Shadow s) {
				super.specializeOn(s);
				((BcelShadow) s).initializeForAroundClosure();
			}
		};
	}

	public List<ShadowMunger> getShadowMungers() {
		List<ShadowMunger> ret = new ArrayList<>();
		ret.add(makeConcreteAdvice("before" + "(): call(* *.println(..)) -> static void fluffy.Aspect.before_method_call()"));
		ret.add(makeConcreteAdvice("afterReturning"
				+ "(): call(* *.println(..)) -> static void fluffy.Aspect.afterReturning_method_call()"));

		ret.add(makeConcreteAdvice("before" + "(): execution(* *.*(..)) -> static void fluffy.Aspect.ignoreMe()"));

		ret.add(makeConcreteAdvice("afterReturning" + "(): execution(* *.*(..)) -> static void fluffy.Aspect.ignoreMe()"));

		ret.add(makeConcreteAdvice("afterThrowing"
				+ "(): execution(* *.*(..)) -> static void fluffy.Aspect.afterThrowing_method_execution(java.lang.Throwable)", 1));
		ret.add(makeConcreteAdvice("after" + "(): execution(* *.*(..)) -> static void fluffy.Aspect.ignoreMe()"));

		ret.add(makeAroundMunger(true));
		return ret;
	}

	public void zipTest(String fileName) throws IOException {
		long startTime = System.currentTimeMillis();
		File inFile = new File(WeaverTestCase.TESTDATA_PATH, fileName);
		File outFile = new File(outDir, fileName);
		outFile.delete();

		world = new BcelWorld("c:/apps/java-1.3.1_04/lib/tools.jar");
		BcelWeaver weaver1 = new BcelWeaver(world);

		ZipFileWeaver weaver = new ZipFileWeaver(inFile);

		weaver1.setShadowMungers(getShadowMungers());

		weaver.weave(weaver1, outFile);
		assertTrue(outFile.lastModified() > startTime);
	}

	public void testEmptyForAntJUnit() {
	}

	// this is something we test every now and again.
	// to try, rename as testBig and put aspectjtools.jar in testdata
	public void trytestBig() throws IOException {
		System.out.println("could take 80 seconds...");
		zipTest("aspectjtools.jar");
	}

}
