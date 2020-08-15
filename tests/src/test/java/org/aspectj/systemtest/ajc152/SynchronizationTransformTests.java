/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andy Clement - initial implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc152;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.testing.util.TestUtil;
import org.aspectj.testing.util.TestUtil.LineStream;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelObjectType;
import org.aspectj.weaver.bcel.BcelWorld;
import org.aspectj.weaver.bcel.LazyClassGen;
import org.aspectj.weaver.bcel.LazyMethodGen;

import junit.framework.Test;

/**
 * Method transformation, example:
 * 
 * public synchronized void m(); Code: Stack=2, Locals=1, Args_size=1 0: getstatic #2; //Field
 * java/lang/System.err:Ljava/io/PrintStream; 3: ldc #3; //String hello 5: invokevirtual #4; //Method
 * java/io/PrintStream.println:(Ljava/lang/String;)V 8: getstatic #2; //Field java/lang/System.err:Ljava/io/PrintStream; 11: ldc #5;
 * //String world 13: invokevirtual #4; //Method java/io/PrintStream.println:(Ljava/lang/String;)V 16: return LineNumberTable: line
 * 4: 0 line 5: 8 line 6: 16
 * 
 * public void m2(); Code: Stack=2, Locals=3, Args_size=1 0: aload_0 1: dup 2: astore_1 3: monitorenter 4: getstatic #2; //Field
 * java/lang/System.err:Ljava/io/PrintStream; 7: ldc #3; //String hello 9: invokevirtual #4; //Method
 * java/io/PrintStream.println:(Ljava/lang/String;)V 12: getstatic #2; //Field java/lang/System.err:Ljava/io/PrintStream; 15: ldc
 * #5; //String world 17: invokevirtual #4; //Method java/io/PrintStream.println:(Ljava/lang/String;)V 20: aload_1 21: monitorexit
 * 22: goto 30 25: astore_2 26: aload_1 27: monitorexit 28: aload_2 29: athrow 30: return Exception table: from to target type 4 22
 * 25 any 25 28 25 any
 */

public class SynchronizationTransformTests extends XMLBasedAjcTestCase {

	private static boolean regenerate;

	static {
		regenerate = false;
	}

	private World world;

	public void testInvestigatingTransforming() {
		runTest("investigation");
		checkMethod("Investigation", "b"); // similar output to One.b
		checkMethod("Investigation", "c");
		checkMethod("Investigation", "d");
		checkMethod("Investigation", "e");
	}

	public void testTransform1() {
		runTest("One");
		checkMethod("One", "b");
		checkMethod("One", "c");
		checkMethod("One", "e");
	}

	// before() on execution jp
	public void testTransform2() {
		runTest("Two");
		checkMethod("C", "ma");
	}

	public void testTransform2XlintOff() {
		runTest("Two - xlintoff");
		checkMethod("C", "ma");
	}

	// after() returning/after() throwing on execution jp
	// after() returning -> make all returns go through the same exit point and make
	// it call the advice
	// after() throwing -> add a catch block that calls the advice
	public void testTransform3() {
		runTest("Three");
		checkMethod("C", "m3");
		checkMethod("C", "m32");
		checkMethod("C", "m33"); // like m() but synchronized block
		checkMethod("C", "m34"); // like m2() but synchronized block
	}

	// like testTransform3() but pointcuts explicitly specify synchronized
	public void testTransform4() {
		runTest("Four");
		checkMethod("C", "m");
		checkMethod("C", "m2");
	}

	// Java5 variant
	public void testStaticSynchronizedMethodTransformJava5() {
		runTest("Five - Java5");
		checkMethod("C", "b");
	}

	// < Java5 variant
	public void testStaticSynchronizedMethodTransformPreJava5() {
		runTest("Six - preJava5");
		checkMethod("C", "bbb");
	}

	public void testLockPcdOnTransformedNonStaticMethod() {
		runTest("lock pcd on transformed non-static method");
	}

	public void testUnlockPcdOnTransformedNonStaticMethod() {
		runTest("unlock pcd on transformed non-static method");
	}

	public void testLockPcdOnTransformedStaticMethod() {
		runTest("lock pcd on transformed static method - J5");
	}

	public void testUnlockPcdOnTransformedStaticMethod() {
		runTest("unlock pcd on transformed static method - J5");
	}

	public void testLockPcdOnTransformedStaticMethodPreJ5() {
		runTest("lock pcd on transformed static method - preJ5");
	}

	public void testUnlockPcdOnTransformedStaticMethodPreJ5() {
		runTest("unlock pcd on transformed static method - preJ5");
	}

	public void testJoinpointsEnabledButNoLock() {
		runTest("joinpoints enabled but no lock");
	}

	public void testTransformWithLTW() {
		runTest("transform with LTW");
	}

	public void testTransformStaticMethodPreJava5() {
		runTest("transform static method - preJ5");
	}

	public void testTransformStaticMethodPreJava5_2() {
		runTest("transform static method - packages - preJ5");
	}

	// more complex code sequences...
	public void testOtherTargeters() {
		runTest("other targeters");
	}

	// --- infrastructure below

	private void checkMethod(String typename, String methodname) {
		LazyMethodGen m = getMethod(typename, methodname);
		File expectedF = new File(".." + File.separator + "tests" + File.separator + "features152" + File.separator
				+ "synchronization" + File.separator + "transformed" + File.separator + "expected" + File.separator + typename
				+ "." + methodname + ".txt");
		if (regenerate) {
			saveMethod(expectedF, m);
		} else {
			compareMethod(expectedF, m);
		}
	}

	private LazyMethodGen getMethod(String typename, String methodname) {
		BcelObjectType type = getBcelObjectFor(typename);
		LazyClassGen lcg = type.getLazyClassGen();
		List<LazyMethodGen> methods = lcg.getMethodGens();
		for (LazyMethodGen element: methods) {
			if (element.getName().equals(methodname)) {
				return element;
			}
		}
		return null;
	}

	private BcelObjectType getBcelObjectFor(String clazzname) {
		ensureWorldSetup();
		ResolvedType rt = world.resolve(clazzname);
		if (rt == null)
			fail("Couldn't find class " + clazzname);
		ReferenceType rtt = (ReferenceType) rt;
		BcelObjectType bot = (BcelObjectType) rtt.getDelegate();
		return bot;
	}

	private void ensureWorldSetup() {
		if (world == null) {
			world = new BcelWorld(getSandboxDirectory() + File.pathSeparator + System.getProperty("java.class.path"));
		}
	}

	protected Method getMethod(JavaClass cl, String methodname) {
		Method[] methods = cl.getMethods();
		for (Method m : methods) {
			if (m.getName().equals(methodname)) {
				return m;
			}
		}
		return null;
	}

	public void dump(String title, String[] strs) {
		System.err.println(title);
		for (int i = 0; i < strs.length; i++) {
			System.err.println(i + ") " + strs[i]);
		}
	}

	private void compareMethod(File f, LazyMethodGen m) {
		BufferedReader fr;
		if (!f.exists()) {
			fail("Can't find expected output file " + f);
		}
		try {
			// Load the file in
			fr = new BufferedReader(new FileReader(f));
			String line = null;
			List<String> originalFileContents = new ArrayList<>();
			while ((line = fr.readLine()) != null)
				originalFileContents.add(line);
			String[] fileContents = (String[]) originalFileContents.toArray(new String[] {});

			LineStream ls = new TestUtil.LineStream();
			m.print(ls, null);
			String[] lines = ls.getLines();
			for (int i = 0; i < lines.length; i++) {
				String existingLine = lines[i];
				if (!fileContents[i].contains("MethodDeclarationLineNumber") && !fileContents[i].equals(existingLine)) {
					dump("File contents:", fileContents);
					dump("Actual:", lines);
					fail("\nDifference in method " + m.getName() + " on line " + i + " between the expected:\n" + fileContents[i]
							+ "\nand the found:\n" + existingLine);
				}
			}
		} catch (Exception e) {
			fail("Unexpected exception saving weaving messages:" + e);
		}
	}

	private String stringify(List<String> l) {
		StringBuffer result = new StringBuffer();
		for (String str : l) {
			result.append(str);
			result.append("\n");
		}
		return result.toString();
	}

	private void saveMethod(File f, LazyMethodGen m) {
		System.out.println("Saving method into " + f.getName());
		try {
			m.print(new PrintStream(new FileOutputStream(f)), null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail("Couldn't store the method in file " + f);
		}
	}

	// --- helpers

	// Half finished - could check there is only one relationship for unlock() rather than two - but
	// that seems to be the case anyway (peculiar...)
	// private void checkModel1() {
	// // Verifies only one unlock relationship, not two
	// IProgramElement unlockNode =
	// AsmManager.getDefault().getHierarchy().findElementForLabel(AsmManager.getDefault().getHierarchy().getRoot(),
	// IProgramElement.Kind.CODE,"unlock(void java.lang.Object.<unlock>(java.lang.Object))");
	// assertTrue("Couldn't find the unlock node",unlockNode!=null);
	// List l = AsmManager.getDefault().getRelationshipMap().get(unlockNode);
	// assertTrue("should be one entry :"+l,l!=null && l.size()==1);
	// IRelationship ir = (IRelationship)l.get(0);
	// System.err.println(ir);
	// List targs = ir.getTargets();
	// System.err.println(targs.size());
	// System.err.println(targs.get(0));
	// }

	// ---
	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(SynchronizationTransformTests.class);
	}

	protected URL getSpecFile() {
    return getClassResource("synchronization.xml");
	}

	public void tearDown() {
		world = null;
	}

}
