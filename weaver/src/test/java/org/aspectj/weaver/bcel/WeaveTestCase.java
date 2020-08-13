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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.InvokeInstruction;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.testing.util.TestUtil;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.WeaverTestCase;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.SimpleScope;

import junit.framework.TestCase;

public abstract class WeaveTestCase extends TestCase {

	public boolean regenerate = false;
	public boolean runTests = true;
	public boolean behave15 = false;

	File outDir;
	String outDirPath;

	public BcelWorld world = new BcelWorld();
	{
		world.addPath(classDir);
		// Some of the tests in here rely on comparing output from dumping the delegates - if
		// we are using ASM delegates we don't know the names of parameters (they are irrelevant...)
		// and are missing from the dumping of asm delegates. This switch ensures we
		// continue to use BCEL for these tests.
		// world.setFastDelegateSupport(false);
	}

	public WeaveTestCase(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		outDir = WeaverTestCase.getOutdir();
		outDirPath = outDir.getAbsolutePath();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		WeaverTestCase.removeOutDir();
		outDir = null;
		outDirPath = null;
	}

	public static InstructionList getAdviceTag(BcelShadow shadow, String where) {
		String methodName = "ajc_" + where + "_" + shadow.getKind().toLegalJavaIdentifier();

		InstructionFactory fact = shadow.getFactory();
		InvokeInstruction il = fact.createInvoke("Aspect", methodName, Type.VOID, new Type[] {}, Constants.INVOKESTATIC);
		return new InstructionList(il);
	}

	public void weaveTest(String name, String outName, ShadowMunger planner) throws IOException {
		List<ShadowMunger> l = new ArrayList<>(1);
		l.add(planner);
		weaveTest(name, outName, l);
	}

	// static String classDir = "../weaver/bin";
	static String classDir = WeaverTestCase.TESTDATA_PATH + File.separator + "bin";

	public void weaveTest(String name, String outName, List<ShadowMunger> planners) throws IOException {
		BcelWeaver weaver = new BcelWeaver(world);
		try {
			if (behave15)
				world.setBehaveInJava5Way(true);

			UnwovenClassFile classFile = makeUnwovenClassFile(classDir, name, outDirPath);

			weaver.addClassFile(classFile, false);
			weaver.setShadowMungers(planners);
			weaveTestInner(weaver, classFile, name, outName);
		} finally {
			if (behave15)
				world.setBehaveInJava5Way(false);
		}
	}

	protected void weaveTestInner(BcelWeaver weaver, UnwovenClassFile classFile, String name, String outName) throws IOException {
		// int preErrors = currentResult.errorCount();
		BcelObjectType classType = BcelWorld.getBcelObjectType(world.resolve(classFile.getClassName()));
		LazyClassGen gen = weaver.weave(classFile, classType);
		if (gen == null) {
			// we didn't do any weaving, but let's make a gen anyway
			gen = classType.getLazyClassGen(); // new LazyClassGen(classType);
		}
		try {
			String filenameToUse = findMostRelevantFile(outName);
			checkClass(gen, outDirPath, filenameToUse);
			if (runTests) {
				System.out.println("*******RUNNING: " + outName + "  " + name + " *******");
				TestUtil.runMain(makeClassPath(outDirPath), name);
			}
		} catch (Error e) {
			System.err.println("Comparing to " + outName + ".txt");
			gen.print(System.err);
			throw e;
		} catch (RuntimeException e) {
			gen.print(System.err);
			throw e;
		}
	}

	public String findMostRelevantFile(String name) {
		double version = LangUtil.getVmVersion();
		while (version > 0) {
			String possibleFileName = name+"."+Double.toString(version)+".txt";
			if (new File(TESTDATA_DIR, possibleFileName).exists()) {
				return possibleFileName;
			}
			version--;
		}
		// Use the standard file
		return name+".txt";
	}

	public String makeClassPath(String outDir) {
		return outDir + File.pathSeparator + getTraceJar() + File.pathSeparator + classDir + File.pathSeparator
				+ System.getProperty("java.class.path");
	}

	/**
	 * '/' in the name indicates the location of the class
	 */
	public static UnwovenClassFile makeUnwovenClassFile(String classDir, String name, String outDir) throws IOException {
		File outFile = new File(outDir, name + ".class");
		if (classDir.endsWith(".jar")) {
			String fname = name + ".class";
			UnwovenClassFile ret = new UnwovenClassFile(outFile.getAbsolutePath(), FileUtil.readAsByteArray(FileUtil
					.getStreamFromZip(classDir, fname)));
			return ret;
		} else {
			File inFile = new File(classDir, name + ".class");
			return new UnwovenClassFile(outFile.getAbsolutePath(), FileUtil.readAsByteArray(inFile));
		}
	}

	public void checkClass(LazyClassGen gen, String outDir, String expectedFile) throws IOException {
		if (regenerate)
			genClass(gen, outDir, expectedFile);
		else
			realCheckClass(gen, outDir, expectedFile);
	}

	static final File TESTDATA_DIR = new File(WeaverTestCase.TESTDATA_PATH);

	void genClass(LazyClassGen gen, String outDir, String expectedFile) throws IOException {
		// ClassGen b = getJavaClass(outDir, className);
		FileOutputStream out = new FileOutputStream(new File(TESTDATA_DIR, expectedFile));
		PrintStream ps = new PrintStream(out);
		gen.print(ps);
		ps.flush();

	}

	void realCheckClass(LazyClassGen gen, String outDir, String expectedFile) throws IOException {
		TestUtil.assertMultiLineStringEquals(expectedFile/* "classes" */,
				FileUtil.readAsString(new File(TESTDATA_DIR, expectedFile)), gen.toLongString());
	}

	// ----
	public ShadowMunger makeConcreteAdvice(String mungerString) {
		return makeConcreteAdvice(mungerString, 0, null);
	}

	public ShadowMunger makeConcreteAdvice(String mungerString, int extraArgFlag) {
		return makeConcreteAdvice(mungerString, extraArgFlag, null);
	}

	protected ShadowMunger makeConcreteAdvice(String mungerString, int extraArgFlag, PerClause perClause) {
		Advice myMunger = BcelTestUtils.shadowMunger(world, mungerString, extraArgFlag);

		// PerSingleton s = new PerSingleton();
		// s.concretize(world.resolve("Aspect"));
		// System.err.println(((KindedPointcut)myMunger.getPointcut().getPointcut()).getKind());
		Advice cm = (Advice) myMunger.concretize(myMunger.getDeclaringAspect().resolve(world), world, perClause);
		return cm;
	}

	public ShadowMunger makeAdviceField(String kind, String extraArgType) {
		return makeConcreteAdvice(kind + "(): get(* *.*) -> static void Aspect.ajc_" + kind + "_field_get(" + extraArgType + ")", 1);
	}

	public List<ShadowMunger> makeAdviceAll(String kind, boolean matchOnlyPrintln) {
		List<ShadowMunger> ret = new ArrayList<>();
		if (matchOnlyPrintln) {
			ret.add(makeConcreteAdvice(kind + "(): call(* *.println(..)) -> static void Aspect.ajc_" + kind + "_method_execution()"));
		} else {
			ret.add(makeConcreteAdvice(kind + "(): call(* *.*(..)) -> static void Aspect.ajc_" + kind + "_method_call()"));
			ret.add(makeConcreteAdvice(kind + "(): call(*.new(..)) -> static void Aspect.ajc_" + kind + "_constructor_call()"));
			ret.add(makeConcreteAdvice(kind + "(): execution(* *.*(..)) -> static void Aspect.ajc_" + kind + "_method_execution()"));
			ret.add(makeConcreteAdvice(kind + "(): execution(*.new(..)) -> static void Aspect.ajc_" + kind
					+ "_constructor_execution()"));
			// ret.add(
			// makeConcreteMunger(
			// kind
			// + "(): staticinitialization(*) -> static void Aspect.ajc_"
			// + kind
			// + "_staticinitialization()"));
			ret.add(makeConcreteAdvice(kind + "(): get(* *.*) -> static void Aspect.ajc_" + kind + "_field_get()"));
			// ret.add(
			// makeConcreteMunger(
			// kind + "(): set(* *.*) -> static void Aspect.ajc_" + kind + "_field_set()"));
			// XXX no test for advice execution, staticInitialization or (god help us) preInitialization
		}
		return ret;
	}

	public List<ShadowMunger> makeAdviceAll(final String kind) {
		return makeAdviceAll(kind, false);
	}

	public Pointcut makePointcutAll() {
		return makeConcretePointcut("get(* *.*) || call(* *.*(..)) || execution(* *.*(..)) || call(*.new(..)) || execution(*.new(..))");
	}

	public Pointcut makePointcutNoZeroArg() {
		return makeConcretePointcut("call(* *.*(*, ..)) || execution(* *.*(*, ..)) || call(*.new(*, ..)) || execution(*.new(*, ..))");
	}

	public Pointcut makePointcutPrintln() {
		return makeConcretePointcut("call(* *.println(..))");
	}

	public Pointcut makeConcretePointcut(String s) {
		return makeResolvedPointcut(s).concretize(null, null, 0);
	}

	public Pointcut makeResolvedPointcut(String s) {
		Pointcut pointcut0 = Pointcut.fromString(s);
		return pointcut0.resolve(new SimpleScope(world, FormalBinding.NONE));
	}

	// ----

	public String[] getStandardTargets() {
		return new String[] { "HelloWorld", "FancyHelloWorld" };
	}

	public String getTraceJar() {
		return WeaverTestCase.TESTDATA_PATH + "/tracing.jar";
	}

	// ----

	protected void weaveTest(String[] inClassNames, String outKind, ShadowMunger patternMunger) throws IOException {
		for (String inFileName : inClassNames) {
			weaveTest(inFileName, outKind + inFileName, patternMunger);
		}
	}

	protected void weaveTest(String[] inClassNames, String outKind, List<ShadowMunger> patternMungers) throws IOException {
		for (String inFileName : inClassNames) {
			weaveTest(inFileName, outKind + inFileName, patternMungers);
		}
	}

	protected List<ShadowMunger> addLexicalOrder(List<ShadowMunger> l) {
		int i = 10;
		for (ShadowMunger element: l) {
			((Advice)element).setLexicalPosition(i += 10);
		}
		return l;
	}

	// XXX cut-and-paster from IdWeaveTestCase
	public void checkShadowSet(List l, String[] ss) {
		outer:
		for (String s : ss) {
			// inner:
			for (Iterator j = l.iterator(); j.hasNext(); ) {
				BcelShadow shadow = (BcelShadow) j.next();
				String shadowString = shadow.toString();
				if (shadowString.equals(s)) {
					j.remove();
					continue outer;
				}
			}
			assertTrue("didn't find " + s + " in " + l, false);
		}
		assertTrue("too many things in " + l, l.size() == 0);
	}

}
