/*******************************************************************************
 * Copyright (c) 2014 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc186;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.weaver.tools.ContextBasedMatcher;
import org.aspectj.weaver.tools.DefaultMatchingContext;
import org.aspectj.weaver.tools.FuzzyBoolean;
import org.aspectj.weaver.tools.MatchingContext;
import org.aspectj.weaver.tools.PointcutDesignatorHandler;
import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParser;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc186Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	private class FooDesignatorHandler implements PointcutDesignatorHandler {

		private String askedToParse;
		public boolean simulateDynamicTest = false;

		public String getDesignatorName() {
			return "foo";
		}

		public ContextBasedMatcher parse(String expression) {
			this.askedToParse = expression;
			return new FooPointcutExpression(expression, this.simulateDynamicTest);
		}

		public String getExpressionLastAskedToParse() {
			return this.askedToParse;
		}
	}

	private class FooPointcutExpression implements ContextBasedMatcher {

		private final String beanNamePattern;
		private final boolean simulateDynamicTest;

		public FooPointcutExpression(String beanNamePattern,
				boolean simulateDynamicTest) {
			this.beanNamePattern = beanNamePattern;
			this.simulateDynamicTest = simulateDynamicTest;
		}

		public boolean couldMatchJoinPointsInType(Class aClass) {
			System.out.println("wubble?");
			return true;
		}

		public boolean couldMatchJoinPointsInType(Class aClass,
				MatchingContext context) {
			System.out.println("wibble?");
			if (this.beanNamePattern.equals(context.getBinding("beanName"))) {
				return true;
			} else {
				return false;
			}
		}

		public boolean mayNeedDynamicTest() {
			return this.simulateDynamicTest;
		}

		public FuzzyBoolean matchesStatically(MatchingContext matchContext) {
			System.out.println("wobble?");
			if (this.simulateDynamicTest)
				return FuzzyBoolean.MAYBE;
			if (this.beanNamePattern
					.equals(matchContext.getBinding("beanName"))) {
				return FuzzyBoolean.YES;
			} else {
				return FuzzyBoolean.NO;
			}
		}

		public boolean matchesDynamically(MatchingContext matchContext) {
			System.out.println("wabble?");
			return this.beanNamePattern.equals(matchContext
					.getBinding("beanName"));
		}
	}

	public void testLambdaBeans() throws Exception {
		runTest("lambda beans");
		
		// Load the 1.8 compiled code
		URLClassLoader ucl = new URLClassLoader(new URL[] {ajc.getSandboxDirectory().toURI().toURL()},this.getClass().getClassLoader());
		Class<?> applicationClass = Class.forName("Application",false,ucl);
		assertNotNull(applicationClass);
		Object instance = applicationClass.newInstance();
		Method works = applicationClass.getDeclaredMethod("fromInnerClass");
		works.setAccessible(true);
		Runnable r = (Runnable) works.invoke(instance);		
		// r.getClass().getName() == Application$1
		
		Method fails = applicationClass.getDeclaredMethod("fromLambdaExpression");
		fails.setAccessible(true);
		Runnable r2 = (Runnable) fails.invoke(instance);		
		// r2.getClass().getName() == Application$$Lambda$1/1652149987
		
//		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), "Application");
		PointcutParser parser = PointcutParser
				.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(ucl);
		FooDesignatorHandler beanHandler = new FooDesignatorHandler();
		parser.registerPointcutDesignatorHandler(beanHandler);
		PointcutExpression pc = parser.parsePointcutExpression("foo(myBean)");
		DefaultMatchingContext context = new DefaultMatchingContext();
		pc.setMatchingContext(context);

		context.addContextBinding("beanName", "myBean");
		assertTrue(pc.couldMatchJoinPointsInType(r.getClass()));
		
		context.addContextBinding("beanName", "yourBean");
		assertFalse(pc.couldMatchJoinPointsInType(r.getClass()));

		context.addContextBinding("beanName", "myBean");
		assertTrue(pc.couldMatchJoinPointsInType(r2.getClass()));
		
		context.addContextBinding("beanName", "yourBean");
		assertFalse(pc.couldMatchJoinPointsInType(r2.getClass()));
	}
	
	
	public void testMissingExtends() throws Exception {
		runTest("missing extends on generic target");
	}

	public void testMissingMethod_462821() throws Exception {
		runTest("missing method");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc186Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc186.xml");
	}

}
