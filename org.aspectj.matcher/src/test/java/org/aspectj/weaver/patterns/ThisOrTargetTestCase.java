/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 *               2005 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation
 *     Adrian Colyer, runtime reflection extensions 
 * ******************************************************************/

package org.aspectj.weaver.patterns;

import java.io.IOException;
import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.aspectj.weaver.tools.JoinPointMatch;
import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParameter;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.ShadowMatch;

/**
 * @author hugunin
 * 
 *         To change this generated comment edit the template variable "typecomment": Window>Preferences>Java>Templates. To enable
 *         and disable the creation of type comments go to Window>Preferences>Java>Code Generation.
 */
public class ThisOrTargetTestCase extends TestCase {

	private boolean needToSkip = false;

	/** this condition can occur on the build machine only, and is way too complex to fix right now... */
	private boolean needToSkipPointcutParserTests() {
		try {
			Class.forName("org.aspectj.weaver.reflect.Java15ReflectionBasedReferenceTypeDelegate", false, this.getClass()
					.getClassLoader());// ReflectionBasedReferenceTypeDelegate.class.getClassLoader());
		} catch (ClassNotFoundException cnfEx) {
			return true;
		}
		return false;
	}

	protected void setUp() throws Exception {
		super.setUp();
		needToSkip = needToSkipPointcutParserTests();
	}

	/**
	 * Constructor for PatternTestCase.
	 * 
	 * @param name
	 */
	public ThisOrTargetTestCase(String name) {
		super(name);
	}

	public void testMatchJP() throws Exception {
		if (needToSkip) {
			return;
		}

		PointcutParser parser = PointcutParser
				.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(this.getClass().getClassLoader());
		PointcutExpression thisEx = parser.parsePointcutExpression("this(Exception)");
		PointcutExpression thisIOEx = parser.parsePointcutExpression("this(java.io.IOException)");

		PointcutExpression targetEx = parser.parsePointcutExpression("target(Exception)");
		PointcutExpression targetIOEx = parser.parsePointcutExpression("target(java.io.IOException)");

		Method toString = Object.class.getMethod("toString", new Class[0]);

		checkMatches(thisEx.matchesMethodCall(toString, toString), new Exception(), null, null);
		checkNoMatch(thisIOEx.matchesMethodCall(toString, toString), new Exception(), null, null);
		checkNoMatch(targetEx.matchesMethodCall(toString, toString), new Exception(), new Object(), null);
		checkNoMatch(targetIOEx.matchesMethodCall(toString, toString), new Exception(), new Exception(), null);

		checkMatches(thisEx.matchesMethodCall(toString, toString), new IOException(), null, null);
		checkMatches(thisIOEx.matchesMethodCall(toString, toString), new IOException(), null, null);

		checkNoMatch(thisEx.matchesMethodCall(toString, toString), new Object(), null, null);
		checkNoMatch(thisIOEx.matchesMethodCall(toString, toString), new Exception(), null, null);
		checkMatches(targetEx.matchesMethodCall(toString, toString), new Exception(), new Exception(), null);
		checkNoMatch(targetIOEx.matchesMethodCall(toString, toString), new Exception(), new Exception(), null);

		checkMatches(targetIOEx.matchesMethodCall(toString, toString), new Exception(), new IOException(), null);
	}

	public void testBinding() throws Exception {
		if (needToSkip) {
			return;
		}
		PointcutParser parser = PointcutParser
				.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(this.getClass().getClassLoader());
		PointcutParameter ex = parser.createPointcutParameter("ex", Exception.class);
		PointcutParameter ioEx = parser.createPointcutParameter("ioEx", IOException.class);

		PointcutExpression thisEx = parser.parsePointcutExpression("this(ex)", Exception.class, new PointcutParameter[] { ex });

		PointcutExpression targetIOEx = parser.parsePointcutExpression("target(ioEx)", Exception.class,
				new PointcutParameter[] { ioEx });

		Method toString = Object.class.getMethod("toString", new Class[0]);

		ShadowMatch sMatch = thisEx.matchesMethodCall(toString, toString);
		Exception exceptionParameter = new Exception();
		IOException ioExceptionParameter = new IOException();
		JoinPointMatch jpMatch = null;
		jpMatch = sMatch.matchesJoinPoint(null, null, null);// 318899
		assertFalse(jpMatch.matches());
		jpMatch = sMatch.matchesJoinPoint(exceptionParameter, null, null);
		assertTrue("should match", jpMatch.matches());
		PointcutParameter[] bindings = jpMatch.getParameterBindings();
		assertEquals("one binding", 1, bindings.length);
		assertEquals("should be exceptionParameter", exceptionParameter, bindings[0].getBinding());
		assertEquals("ex", bindings[0].getName());

		sMatch = targetIOEx.matchesMethodCall(toString, toString);
		jpMatch = sMatch.matchesJoinPoint(exceptionParameter, ioExceptionParameter, null);
		assertTrue("should match", jpMatch.matches());
		bindings = jpMatch.getParameterBindings();
		assertEquals("one binding", 1, bindings.length);
		assertEquals("should be ioExceptionParameter", ioExceptionParameter, bindings[0].getBinding());
		assertEquals("ioEx", bindings[0].getName());

	}

	private void checkMatches(ShadowMatch sMatch, Object thisObj, Object targetObj, Object[] args) {
		assertTrue("match expected", sMatch.matchesJoinPoint(thisObj, targetObj, args).matches());
	}

	private void checkNoMatch(ShadowMatch sMatch, Object thisObj, Object targetObj, Object[] args) {
		assertFalse("no match expected", sMatch.matchesJoinPoint(thisObj, targetObj, args).matches());
	}

	/**
	 * Method checkSerialization.
	 * 
	 * @param string
	 */
	// private void checkSerialization(String string) throws IOException {
	// Pointcut p = makePointcut(string);
	// ByteArrayOutputStream bo = new ByteArrayOutputStream();
	// DataOutputStream out = new DataOutputStream(bo);
	// p.write(out);
	// out.close();
	//
	// ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
	// DataInputStream in = new DataInputStream(bi);
	// Pointcut newP = Pointcut.read(in, null);
	//
	// assertEquals("write/read", p, newP);
	// }
}
