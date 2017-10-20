/*******************************************************************************
 * Copyright (c) 2017 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.aspectj.systemtest.ajc190;

import java.io.File;

import junit.framework.Test;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.Code;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * Building and weaving with modules in the picture...
 * 
 * @author Andy Clement
 * 
 */
public class ModuleTests extends org.aspectj.testing.XMLBasedAjcTestCase {

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

	public void testBinaryWeavingAModuleJar() {
		// Pass a module on inpath, does it weave ok with a source aspect, does it run afterwards?
		runTest("binary weaving module");
	}

	// can't really write these tests now... pure jdt seems to allow type resolution against module path for types
	// not in modules being compiled but javac does not

	public void xtestReferenceTypesFromModuleInBuildingSomeCode() {
		runTest("compile regular code using module code");
	}

	public void xtestReferenceTypesFromModuleInBuildingSomeCodeButCantSeeThem() {
		runTest("compile regular code using module code that isn't visible");
	}

	public void xtestBinaryWeavingInvolvingTypesOnModulePath() {
		fail();
	}

	public void xtestLoadtimeWeavingWithModulePathContainingTypes() {
		fail();
	}
	
	/* For the specified class, check that each method has a stackmap attribute */
	private void checkStackMapExistence(String classname, String toIgnore) throws ClassNotFoundException {
		toIgnore = "_" + (toIgnore == null ? "" : toIgnore) + "_";
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), classname);
		Method[] methods = jc.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
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
		for (int i = 0; i < attrs.length; i++) {
			Attribute attribute = attrs[i];
			if (attribute.getName().equals(attributeName)) {
				return true;
			}
			// System.out.println(attribute.getName());
			if (attribute.getName().equals("Code")) {
				Code c = (Code) attribute;
				Attribute[] codeAttributes = c.getAttributes();
				for (int j = 0; j < codeAttributes.length; j++) {
					Attribute codeAttribute = codeAttributes[j];
					if (codeAttribute.getName().equals(attributeName)) {
						return true;
						// System.out.println(codeAttribute.getName());
					}
				}
			}
		}
		return false;
	}

	private void checkVersion(String classname, int major, int minor) throws ClassNotFoundException {
		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), classname);
		if (jc.getMajor() != major) {
			fail("Expected major version to be " + major + " but was " + jc.getMajor());
		}
		if (jc.getMinor() != minor) {
			fail("Expected minor version to be " + minor + " but was " + jc.getMinor());
		}
	}

	// Check the stackmap stuff is removed when a method gets woven (for now...)
	// public void testStackMapAttributesDeletedInWovenCode() {
	// fail("Not implemented");
	// }

	// ///////////////////////////////////////
	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(ModuleTests.class);
	}

	protected File getSpecFile() {
		return getClassResource("ajc190.xml");
	}

}
