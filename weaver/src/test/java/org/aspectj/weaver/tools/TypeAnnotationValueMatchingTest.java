/* *******************************************************************
 * Copyright (c) 2026 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 * ******************************************************************/
package org.aspectj.weaver.tools;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import junit.framework.TestCase;

/**
 * Matching annotation values on types when pointcuts are matched by reflection, on a type declaring the
 * annotation and on a subclass inheriting it.
 */
public class TypeAnnotationValueMatchingTest extends TestCase {

	private static final String PATTERN = "within(@" + TypeAnnotationValueMatchingTest.class.getName()
			+ ".InheritedAnnotation(\"%s\") *)";

	public void testAnnotationValuesMatchAType() throws Exception {
		assertTrue(matches(String.format(PATTERN, "base"), Base.class));
		assertFalse(matches(String.format(PATTERN, "other"), Base.class));
	}

	public void testInheritedAnnotationValuesMatchASubclass() throws Exception {
		assertTrue(matches(String.format(PATTERN, "base"), Subclass.class));
		assertFalse(matches(String.format(PATTERN, "other"), Subclass.class));
	}

	private boolean matches(String expression, Class<?> type) throws Exception {
		PointcutParser parser = PointcutParser
				.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(getClass().getClassLoader());
		return parser.parsePointcutExpression(expression).matchesMethodExecution(type.getMethod("run")).alwaysMatches();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@interface InheritedAnnotation {
		String value();
	}

	@InheritedAnnotation("base")
	public static class Base {
		public void run() {
		}
	}

	public static class Subclass extends Base {
		@Override
		public void run() {
		}
	}
}
