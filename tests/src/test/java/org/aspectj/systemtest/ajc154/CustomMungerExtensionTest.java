/* *******************************************************************
 * Copyright (c) 2007 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Linton Ye https://bugs.eclipse.org/bugs/show_bug.cgi?id=193065
 * ******************************************************************/

package org.aspectj.systemtest.ajc154;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.aspectj.ajde.core.AjCompiler;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.systemtest.incremental.tools.AjdeInteractionTestbed;
import org.aspectj.weaver.Checker;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.CustomMungerFactory;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.DeclareErrorOrWarning;
import org.aspectj.weaver.patterns.IfPointcut;
import org.aspectj.weaver.patterns.Pointcut;

public class CustomMungerExtensionTest extends AjdeInteractionTestbed {

	File oldSandBoxDir;

	protected void setUp() throws Exception {
		super.setUp();
		oldSandBoxDir = sandboxDir;
		sandboxDir = new File("../tests");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		sandboxDir = oldSandBoxDir;
	}

	public void testExtension() {
		String testFileDir = "bugs/pointcutdoctor-bug193065";
		AjCompiler compiler = getCompilerForProjectWithName(testFileDir);
		compiler.setCustomMungerFactory(new DumbCustomMungerFactory());

		doBuild(testFileDir);

		CustomMungerFactory factory = (CustomMungerFactory) compiler.getCustomMungerFactory();
		assertTrue(factory.getAllCreatedCustomShadowMungers().size() > 0);
		for (ShadowMunger shadowMunger : factory.getAllCreatedCustomShadowMungers()) {
			assertTrue(((DumbShadowMunger) shadowMunger).called);
		}

		assertTrue(factory.getAllCreatedCustomTypeMungers().size() > 0);
		for (ConcreteTypeMunger concreteTypeMunger : factory.getAllCreatedCustomTypeMungers()) {
			assertTrue(((DumbTypeMunger) concreteTypeMunger).called);
		}
	}

	class DumbCustomMungerFactory implements CustomMungerFactory {
		Collection<ShadowMunger> allShadowMungers = new ArrayList<>();
		Collection<ConcreteTypeMunger> allTypeMungers = new ArrayList<>();

		public Collection<ShadowMunger> createCustomShadowMungers(ResolvedType aspectType) {
			List<ShadowMunger> mungers = new ArrayList<>();
			Pointcut pointcut = new IfPointcut("abc");
			mungers.add(new DumbShadowMunger(new DeclareErrorOrWarning(false, pointcut, "")));
			allShadowMungers.addAll(mungers);
			return mungers;
		}

		public Collection<ConcreteTypeMunger> createCustomTypeMungers(ResolvedType aspectType) {
			List<ConcreteTypeMunger> mungers = new ArrayList<>();
			mungers.add(new DumbTypeMunger(null, aspectType));
			allTypeMungers.addAll(mungers);
			return mungers;
		}

		public Collection<ShadowMunger> getAllCreatedCustomShadowMungers() {
			return allShadowMungers;
		}

		public Collection<ConcreteTypeMunger> getAllCreatedCustomTypeMungers() {
			return allTypeMungers;
		}
	}

	class DumbShadowMunger extends Checker {
		public DumbShadowMunger(DeclareErrorOrWarning deow) {
			super(deow);
		}

		public ISourceLocation getSourceLocation() {
			return ISourceLocation.EMPTY;
		}

		boolean called;

		public boolean match(Shadow shadow, World world) {
			called = true;
			return false;
		}
	}

	class DumbTypeMunger extends ConcreteTypeMunger {
		boolean called;

		public DumbTypeMunger(ResolvedTypeMunger munger, ResolvedType aspectType) {
			super(munger, aspectType);
		}

		public ConcreteTypeMunger parameterizedFor(ResolvedType targetType) {
			return null;
		}

		public boolean matches(ResolvedType onType) {
			called = true;
			return false;
		}

		public ConcreteTypeMunger parameterizeWith(Map parameterizationMap, World world) {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
