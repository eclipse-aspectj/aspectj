/*******************************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.aspectj.systemtest.ajc1927;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.PermittedSubclasses;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.WeaverStateInfo;
import org.aspectj.weaver.bcel.BcelConstantPoolReader;
import org.aspectj.weaver.bcel.BcelWorld;
import org.aspectj.weaver.bcel.Utility;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Bugs1927Tests extends XMLBasedAjcTestCase {

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Bugs1927Tests.class);
	}

	// Only the name of the source file of an aspect is recorded, not the directory it was built from
	public void testGh373_SourceFileDirectoryNotRecorded() throws Exception {
		runTest("class files of an aspect do not record the directory of its source file");

		BcelWorld world = new BcelWorld(ajc.getSandboxDirectory().getPath());
		ResolvedType audit = world.resolve("Audit");
		JavaClass auditClass = getClassFrom(ajc.getSandboxDirectory(), "Audit");

		// the SourceContext attribute of the aspect
		List<AjAttribute> attributes = Utility.readAjAttributes("Audit", auditClass.getAttributes(), audit.getSourceContext(),
				world, AjAttribute.WeaverVersionInfo.UNKNOWN, new BcelConstantPoolReader(auditClass.getConstantPool()));
		AjAttribute.SourceContextAttribute sourceContext = null;
		for (AjAttribute attribute : attributes) {
			if (attribute instanceof AjAttribute.SourceContextAttribute) {
				sourceContext = (AjAttribute.SourceContextAttribute) attribute;
			}
		}
		assertNotNull(sourceContext);
		assertEquals("Audit.aj", sourceContext.getSourceFileName());

		// the source location of the inter-type declaration, in the aspect and in the class it was woven into
		assertEquals(1, audit.getTypeMungers().size());
		for (ConcreteTypeMunger munger : audit.getTypeMungers()) {
			assertEquals("Audit.aj", munger.getMunger().getSourceLocation().getSourceFile().getPath());
		}
		ResolvedType target = world.resolve("Target");
		WeaverStateInfo woven = getWeaverStateInfo(getClassFrom(ajc.getSandboxDirectory(), "Target"));
		assertEquals(1, woven.getTypeMungers(target).size());
		for (ConcreteTypeMunger munger : woven.getTypeMungers(target)) {
			assertEquals("Audit.aj", munger.getMunger().getSourceLocation().getSourceFile().getPath());
		}
	}

	public void testGh357_NoBridgesInConcreteAspects_AbstractAspectLast() throws Exception {
		runTest("no bridge methods in concrete aspects - abstract aspect last");
		assertNoBridgesForTheAbstractAspect();
	}

	public void testGh357_NoBridgesInConcreteAspects_AbstractAspectInTheMiddle() throws Exception {
		runTest("no bridge methods in concrete aspects - abstract aspect in the middle");
		assertNoBridgesForTheAbstractAspect();
	}

	/**
	 * AbstractAudit is not public in the source but is in the class file, so no concrete aspect needs a bridge for its public
	 * method, whichever order the files were compiled in.
	 */
	private void assertNoBridgesForTheAbstractAspect() throws Exception {
		for (String aspect : new String[] { "ConcreteA", "ConcreteB", "ConcreteC" }) {
			for (Method method : getClassFrom(ajc.getSandboxDirectory(), aspect).getMethods()) {
				assertFalse(aspect + " has a bridge for the method of AbstractAudit: " + method, method.getName().equals("setInterceptor"));
			}
		}
	}

	public void testGh383_OrderOfBridgeMethodsDoesNotDependOnFileOrder() throws Exception {
		runTest("order of bridge methods does not depend on file order - class first");
		List<String> classFirst = bridgesOfC();
		runTest("order of bridge methods does not depend on file order - class last");
		assertEquals(2, classFirst.size());
		assertEquals(classFirst, bridgesOfC());
	}

	public void testGh383_OrderOfPermittedSubclassesDoesNotDependOnFileOrder() throws Exception {
		runTest("order of permitted subclasses of an enum does not depend on file order - enum first");
		List<String> enumFirst = permittedSubclassesOfE();
		runTest("order of permitted subclasses of an enum does not depend on file order - enum last");
		assertEquals(Arrays.asList("e/E$1", "e/E$2"), enumFirst);
		assertEquals(enumFirst, permittedSubclassesOfE());
	}

	/** The bridges, in class file order, that app.C has for the public methods it inherits from the non-public lib.P. */
	private List<String> bridgesOfC() throws Exception {
		List<String> bridges = new ArrayList<>();
		for (Method method : parseFromSandbox("app.C").getMethods()) {
			if (method.getName().equals("create")) {
				bridges.add(method.getSignature());
			}
		}
		return bridges;
	}

	private List<String> permittedSubclassesOfE() throws Exception {
		Attribute permitted = getAttributeStartsWith(parseFromSandbox("e.E").getAttributes(), "PermittedSubclasses");
		return Arrays.asList(((PermittedSubclasses) permitted).getClassesNames());
	}

	/** The repository caches the classes it has loaded, so clear it to see what the latest compile wrote. */
	private JavaClass parseFromSandbox(String className) throws Exception {
		createRepos(ajc.getSandboxDirectory()).clear();
		return getClassFrom(ajc.getSandboxDirectory(), className);
	}

	public void testPermittedSubclassesOfAWovenClassAreKept() throws Exception {
		runTest("PermittedSubclasses attribute of a woven class is kept");
		Attribute permitted = getAttributeStartsWith(getClassFrom(ajc.getSandboxDirectory(), "Shape").getAttributes(), "PermittedSubclasses");
		assertEquals(Arrays.asList("Circle", "Square"), Arrays.asList(((PermittedSubclasses) permitted).getClassesNames()));
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc1927.xml");
	}

}
