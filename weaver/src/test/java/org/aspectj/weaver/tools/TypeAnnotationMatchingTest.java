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
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * Matching annotations on types when pointcuts are matched by reflection. An annotation with runtime
 * retention is read from the class by reflection, one with class retention from its class file; both
 * must match as they do in the class file, which holds only the annotations declared on the class.
 */
public class TypeAnnotationMatchingTest extends TestCase {

	private static final String PREFIX = TypeAnnotationMatchingTest.class.getName() + ".";

	public void testRuntimeRetentionAnnotationOnType() throws Exception {
		assertTrue(matches("within(@" + PREFIX + "RuntimeAnnotation *)", RuntimeAnnotated.class));
		assertTrue(matches("@within(" + PREFIX + "RuntimeAnnotation)", RuntimeAnnotated.class));
		assertFalse(matches("@within(" + PREFIX + "RuntimeAnnotation)", ClassFileAnnotated.class));
	}

	public void testClassRetentionAnnotationOnTypeIsStillFoundInTheClassFile() throws Exception {
		assertTrue(matches("within(@" + PREFIX + "ClassFileAnnotation *)", ClassFileAnnotated.class));
		assertFalse(matches("within(@" + PREFIX + "ClassFileAnnotation *)", RuntimeAnnotated.class));
	}

	public void testInheritedAnnotationMatchesASubclass() throws Exception {
		assertTrue(matches("@within(" + PREFIX + "InheritedAnnotation)", InheritingSubclass.class));
	}

	public void testAnnotationThatIsNotInheritedDoesNotMatchASubclass() throws Exception {
		assertTrue(matches("@within(" + PREFIX + "RuntimeAnnotation)", RuntimeAnnotated.class));
		assertFalse(matches("@within(" + PREFIX + "RuntimeAnnotation)", NotInheritingSubclass.class));
	}

	/**
	 * Before, reading a type's annotations always parsed its class file, although reflection on the
	 * loaded class answers for an annotation with runtime retention.
	 */
	public void testRuntimeRetentionAnnotationOnTypeIsMatchedWithoutReadingClassFiles() throws Exception {
		ClassFileRequestCountingLoader loader = new ClassFileRequestCountingLoader(getClass().getClassLoader());
		PointcutParser parser = PointcutParser
				.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(loader);

		ShadowMatch match = parser.parsePointcutExpression("@within(" + PREFIX + "RuntimeAnnotation)")
				.matchesMethodExecution(RuntimeAnnotated.class.getMethod("run"));

		assertTrue(match.alwaysMatches());
		assertEquals("class files read", new ArrayList<String>(), loader.classFilesOf(TypeAnnotationMatchingTest.class));
	}

	private boolean matches(String expression, Class<?> type) throws Exception {
		PointcutParser parser = PointcutParser
				.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(getClass().getClassLoader());
		Method method = type.getMethod("run");
		ShadowMatch match = parser.parsePointcutExpression(expression).matchesMethodExecution(method);
		assertFalse(expression + " on " + type.getSimpleName() + " should be decided statically", match.maybeMatches()
				&& !match.alwaysMatches());
		return match.alwaysMatches();
	}

	/**
	 * Records the class files requested as resources.
	 */
	private static class ClassFileRequestCountingLoader extends ClassLoader {

		private final List<String> requested = new ArrayList<>();

		ClassFileRequestCountingLoader(ClassLoader parent) {
			super(parent);
		}

		@Override
		public URL getResource(String name) {
			if (name.endsWith(".class")) {
				requested.add(name);
			}
			return super.getResource(name);
		}

		/**
		 * The requested class files of the given class and the classes nested in it.
		 */
		List<String> classFilesOf(Class<?> outer) {
			String path = outer.getName().replace('.', '/');
			List<String> result = new ArrayList<>();
			for (String name : requested) {
				if (name.startsWith(path)) {
					result.add(name);
				}
			}
			return result;
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface RuntimeAnnotation {
	}

	// class retention
	@interface ClassFileAnnotation {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@interface InheritedAnnotation {
		String value();
	}

	@RuntimeAnnotation
	public static class RuntimeAnnotated {
		public void run() {
		}
	}

	public static class NotInheritingSubclass extends RuntimeAnnotated {
		@Override
		public void run() {
		}
	}

	@ClassFileAnnotation
	public static class ClassFileAnnotated {
		public void run() {
		}
	}

	@InheritedAnnotation("base")
	public static class InheritingBase {
		public void run() {
		}
	}

	public static class InheritingSubclass extends InheritingBase {
		@Override
		public void run() {
		}
	}
}
