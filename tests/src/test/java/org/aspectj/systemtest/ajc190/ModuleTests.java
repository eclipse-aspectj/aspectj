/*******************************************************************************
 * Copyright (c) 2017 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.aspectj.systemtest.ajc190;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.Code;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCaseForJava9OrLater;
import org.aspectj.util.LangUtil;

import junit.framework.Test;

/**
 * Building and weaving with modules in the picture.
 * 
 * Module options from http://openjdk.java.net/jeps/261
 * 
 * @author Andy Clement
 * 
 */
public class ModuleTests extends XMLBasedAjcTestCaseForJava9OrLater {

	public void testBuildAModule() {
		runTest("build a module");
	}

	public void testRunModuleClassPath() {
		runTest("run a module - classpath");
	}

	public void testRunModuleModulePath() {
		runTest("run a module - modulepath");
	}
	
	public void testPackageAndRunModuleFromModulePath() {
		runTest("package and run a module - modulepath");
	}

	public void testBuildModuleIncludingAspects() {
		runTest("compile module including aspects");
	}
	
	public void testBuildModuleAndApplyAspectsFromAspectPath() {
		runTest("compile module and apply aspects via aspectpath");
	}

	public void testBinaryWeavingAModuleJar() {
		// Pass a module on inpath, does it weave ok with a source aspect, does it run afterwards?
		runTest("binary weaving module");
	}

	public void testModulepathClasspathResolution1() {
		runTest("module path vs classpath 1");
	}

//	public void testModulepathClasspathResolution2() {
//		runTest("module path vs classpath 2");
//	}
	
	// --add-modules
	
	// This tests that when using --add-modules with one of the JDK modules (in the jmods subfolder of the JDK)
	// that it can be found without needing to set --module-path (this seems to be implicitly included by javac too)
	public void testAddModules1() {
		if (LangUtil.is11VMOrGreater()) {
			// java.xml.bind is gone in Java11
			return;
		}
		runTest("compile use of java.xml.bind");
	}
	
	// This tests that we can use add-modules to pull in something from the JDK jmods package and that
	// when subsequently weaving we can see types from those modules
	public void testWovenAfterAddModules() {
		if (LangUtil.is11VMOrGreater()) {
			// java.xml.bind is gone in Java11
			return;
		}
		runTest("weave use of java.xml.bind");
	}
	
	// --limit-modules
	public void testLimitModules1() {
		if (LangUtil.is11VMOrGreater()) {
			// java.xml.bind is gone in Java11
			return;
		}
		runTest("limit modules 1");
	}

	// --add-reads
	public void testAddReads1() {
		if (LangUtil.is11VMOrGreater()) {
			// java.xml.bind is gone in Java11
			return;
		}
		runTest("add reads 1");
	}
	
	
	// ---

	/* For the specified class, check that each method has a stackmap attribute */
	private void checkStackMapExistence(String classname, String toIgnore) throws ClassNotFoundException {
		toIgnore = "_" + (toIgnore == null ? "" : toIgnore) + "_";
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), classname);
		Method[] methods = jc.getMethods();
		for (Method method : methods) {
			if (toIgnore.contains("_" + method.getName() + "_")) {
				continue;
			}
			boolean hasStackMapAttribute = findAttribute(method.getAttributes(), "StackMapTable");
			if (!hasStackMapAttribute) {
				fail("Could not find StackMap attribute for method " + method.getName());
			}
		}
	}

	private boolean findAttribute(Attribute[] attrs, String attributeName) {
		if (attrs == null) {
			return false;
		}
		for (Attribute attribute : attrs) {
			if (attribute.getName().equals(attributeName)) {
				return true;
			}
			// System.out.println(attribute.getName());
			if (attribute.getName().equals("Code")) {
				Code c = (Code) attribute;
				Attribute[] codeAttributes = c.getAttributes();
				for (Attribute codeAttribute : codeAttributes) {
					if (codeAttribute.getName().equals(attributeName)) {
						return true;
						// System.out.println(codeAttribute.getName());
					}
				}
			}
		}
		return false;
	}

	// Check the stackmap stuff is removed when a method gets woven (for now...)
	// public void testStackMapAttributesDeletedInWovenCode() {
	// fail("Not implemented");
	// }

	// ///////////////////////////////////////
	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(ModuleTests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc190.xml");
	}

}
