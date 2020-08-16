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

package org.aspectj.weaver.patterns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.reflect.ReflectionWorld;

/**
 * @author hugunin
 * 
 *         To change this generated comment edit the template variable "typecomment": Window>Preferences>Java>Templates. To enable
 *         and disable the creation of type comments go to Window>Preferences>Java>Code Generation.
 */
public class ParserTestCase extends PatternsTestCase {

	public World getWorld() {
		return new ReflectionWorld(true, getClassLoaderForFile(getTestDataJar()));
	}

	public void testNamePatterns() {

		// checkNoMatch("abc *", "abcd");
		// checkNoMatch("* d", "abcd");
	}

	public void testParse() {
		PatternParser parser = new PatternParser("execution(void Hello.*(..))");
		KindedPointcut p = (KindedPointcut) parser.parsePointcut();
		// System.out.println(p);
		assertEquals(p.kind, Shadow.MethodExecution);
		assertTrue(p.getSignature().getName().matches("foobar"));

		try {
			new PatternParser("initialization(void foo())").parsePointcut();
			fail("should have been a parse error");
		} catch (ParserException pe) {
			// good
		}
	}

	// public void testParseExecutionWithAnnotation() {
	// PatternParser parser = new PatternParser("execution(@SimpleAnnotation void Hello.*(..))");
	// KindedPointcut p = (KindedPointcut) parser.parsePointcut();
	// // XXX - needs finishing...
	// p.resolveBindings(makeSimpleScope(), new Bindings(3));
	// assertEquals("execution(@p.SimpleAnnotation void Hello.*(..))", p.toString());
	// assertEquals(p.kind, Shadow.MethodExecution);
	// assertTrue(p.getSignature().getName().matches("foobar"));
	// }

	// note... toString on a pointcut is a very quick and easy way to test a successful parse
	public void testParseExecutionWithMultipleAnnotations() {
		PatternParser parser = new PatternParser("execution(@SimpleAnnotation (@Foo Integer) (@Goo Hello).*(..))");
		KindedPointcut p = (KindedPointcut) parser.parsePointcut();
		assertEquals("execution(@(SimpleAnnotation) (@(Foo) Integer) (@(Goo) Hello).*(..))", p.toString());
	}

	public void testParseCallWithMultipleAnnotations() {
		PatternParser parser = new PatternParser("call(@SimpleAnnotation (@Foo Integer) (@Goo Hello).*(..))");
		KindedPointcut p = (KindedPointcut) parser.parsePointcut();
		assertEquals("call(@(SimpleAnnotation) (@(Foo) Integer) (@(Goo) Hello).*(..))", p.toString());
	}

	public void testParseGetWithAnnotations() {
		PatternParser parser = new PatternParser("get(@Foo (@SimpleAnnotation ReturnType) (@Foo @Goo Hello).*)");
		KindedPointcut p = (KindedPointcut) parser.parsePointcut();
		assertEquals("get(@(Foo) (@(SimpleAnnotation) ReturnType) (@(Foo) @(Goo) Hello).*)", p.toString());
	}

	public void testParseBadGetWithAnnotations() {
		PatternParser parser = new PatternParser("get(@Foo (@Foo @Goo Hello).*)");
		try {
			// KindedPointcut p = (KindedPointcut)
			parser.parsePointcut();
			fail("Expected parser exception");
		} catch (ParserException pEx) {
			assertEquals("name pattern", pEx.getMessage());
		}
	}

	public void testParseGetWithAndAggregationAnnotations() {
		PatternParser parser = new PatternParser("get(@Foo @SimpleAnnotation ReturnType (@Foo @Goo Hello).*)");
		KindedPointcut p = (KindedPointcut) parser.parsePointcut();
		assertEquals("get(@(Foo) @(SimpleAnnotation) ReturnType (@(Foo) @(Goo) Hello).*)", p.toString());
	}

	public void testParseSetWithAnnotations() {
		PatternParser parser = new PatternParser("set(@Foo (@SimpleAnnotation ReturnType) (@Foo @Goo Hello).*)");
		KindedPointcut p = (KindedPointcut) parser.parsePointcut();
		assertEquals("set(@(Foo) (@(SimpleAnnotation) ReturnType) (@(Foo) @(Goo) Hello).*)", p.toString());
	}

	public void testParseHandlerWithAnnotations() {
		PatternParser parser = new PatternParser("handler(@Critical Exception+)");
		Pointcut p = parser.parsePointcut();
		assertEquals("handler((@(Critical) Exception+))", p.toString());
	}

	public void testParseInitializationWithAnnotations() {
		PatternParser parser = new PatternParser("initialization(@Foo (@Goo Hello).new(@Foo Integer))");
		Pointcut p = parser.parsePointcut();
		assertEquals("initialization(@(Foo) (@(Goo) Hello).new((@(Foo) Integer)))", p.toString());

	}

	public void testParsePreInitializationWithAnnotations() {
		PatternParser parser = new PatternParser("preinitialization(@Foo (@Goo Hello).new(@Foo Integer))");
		Pointcut p = parser.parsePointcut();
		assertEquals("preinitialization(@(Foo) (@(Goo) Hello).new((@(Foo) Integer)))", p.toString());
	}

	public void testStaticInitializationWithAnnotations() {
		PatternParser parser = new PatternParser("staticinitialization(@Foo @Boo @Goo Moo)");
		Pointcut p = parser.parsePointcut();
		assertEquals("staticinitialization((@(Foo) @(Boo) @(Goo) Moo).<clinit>())", p.toString());
	}

	public void testWithinWithAnnotations() {
		PatternParser parser = new PatternParser("within(@Foo *)");
		Pointcut p = parser.parsePointcut();
		assertEquals("within((@(Foo) *))", p.toString());
	}

	public void testWithinCodeWithAnnotations() {
		PatternParser parser = new PatternParser("withincode(@Foo * *.*(..))");
		Pointcut p = parser.parsePointcut();
		assertEquals("withincode(@(Foo) * *.*(..))", p.toString());
	}

	public void testAtAnnotation() {
		PatternParser parser = new PatternParser("@annotation(Foo)");
		AnnotationPointcut p = (AnnotationPointcut) parser.parsePointcut();
		assertEquals("@annotation(Foo)", p.toString());
	}

	public void testBadAtAnnotation() {
		PatternParser parser = new PatternParser("@annotation(!Foo)");
		try {
			// Pointcut p =
			parser.parsePointcut();
			fail("Expected parser exception");
		} catch (ParserException pEx) {
			assertEquals("identifier", pEx.getMessage());
		}
	}

	public void testAtAnnotationWithBinding() {
		PatternParser parser = new PatternParser("@annotation(foo)");
		AnnotationPointcut p = (AnnotationPointcut) parser.parsePointcut();
		assertEquals("@annotation(foo)", p.toString());
	}

	public void testDoubleAtAnnotation() {
		PatternParser parser = new PatternParser("@annotation(Foo Goo)");
		try {
			// Pointcut p =
			parser.parsePointcut();
			fail("Expected parser exception");
		} catch (ParserException pEx) {
			assertEquals(")", pEx.getMessage());
		}
	}

	public void testAtWithin() {
		PatternParser parser = new PatternParser("@within(foo)");
		WithinAnnotationPointcut p = (WithinAnnotationPointcut) parser.parsePointcut();
		assertEquals("@within(foo)", p.toString());
		parser = new PatternParser("@within(Foo))");
		p = (WithinAnnotationPointcut) parser.parsePointcut();
		assertEquals("@within(Foo)", p.toString());
	}

	public void testAtWithinCode() {
		PatternParser parser = new PatternParser("@withincode(foo)");
		WithinCodeAnnotationPointcut p = (WithinCodeAnnotationPointcut) parser.parsePointcut();
		assertEquals("@withincode(foo)", p.toString());
		parser = new PatternParser("@withincode(Foo))");
		p = (WithinCodeAnnotationPointcut) parser.parsePointcut();
		assertEquals("@withincode(Foo)", p.toString());
	}

	public void testAtThis() {
		PatternParser parser = new PatternParser("@this(foo)");
		ThisOrTargetAnnotationPointcut p = (ThisOrTargetAnnotationPointcut) parser.parsePointcut();
		assertEquals("@this(foo)", p.toString());
		assertTrue("isThis", p.isThis());
		parser = new PatternParser("@this(Foo))");
		p = (ThisOrTargetAnnotationPointcut) parser.parsePointcut();
		assertTrue("isThis", p.isThis());
		assertEquals("@this(Foo)", p.toString());
	}

	public void testAtTarget() {
		PatternParser parser = new PatternParser("@target(foo)");
		ThisOrTargetAnnotationPointcut p = (ThisOrTargetAnnotationPointcut) parser.parsePointcut();
		assertEquals("@target(foo)", p.toString());
		assertTrue("isTarget", !p.isThis());
		parser = new PatternParser("@target(Foo))");
		p = (ThisOrTargetAnnotationPointcut) parser.parsePointcut();
		assertTrue("isTarget", !p.isThis());
		assertEquals("@target(Foo)", p.toString());
	}

	public void testAtArgs() {
		PatternParser parser = new PatternParser("@args(Foo,Goo,*,..,Moo)");
		Pointcut p = parser.parsePointcut();
		assertEquals("@args(Foo, Goo, ANY, .., Moo)", p.toString());
	}

	public void testParseSimpleTypeVariable() {
		PatternParser parser = new PatternParser("T");
		TypeVariablePattern tv = parser.parseTypeVariable();
		TypeVariablePattern expected = new TypeVariablePattern("T");
		assertEquals("Expected simple type variable T", expected, tv);
	}

	public void testParseExtendingTypeVariable() {
		PatternParser parser = new PatternParser("T extends Number");
		TypeVariablePattern tv = parser.parseTypeVariable();
		TypeVariablePattern expected = new TypeVariablePattern("T", new PatternParser("Number").parseTypePattern());
		assertEquals("Expected type variable T extends Number", expected, tv);
	}

	public void testParseExtendingTypeVariableWithPattern() {
		PatternParser parser = new PatternParser("T extends Number+");
		TypeVariablePattern tv = parser.parseTypeVariable();
		TypeVariablePattern expected = new TypeVariablePattern("T", new PatternParser("Number+").parseTypePattern());
		assertEquals("Expected type variable T extends Number+", expected, tv);
	}

	public void testParseExtendingTypeVariableWithInterface() {
		PatternParser parser = new PatternParser("T extends Number & Comparable");
		TypeVariablePattern tv = parser.parseTypeVariable();
		TypeVariablePattern expected = new TypeVariablePattern("T", new PatternParser("Number").parseTypePattern(),
				new TypePattern[] { new PatternParser("Comparable").parseTypePattern() }, null);
		assertEquals("Expected type variable T extends Number", expected, tv);
	}

	public void testParseExtendingTypeVariableWithInterfaceList() {
		PatternParser parser = new PatternParser("T extends Number & Comparable & Cloneable");
		TypeVariablePattern tv = parser.parseTypeVariable();
		TypeVariablePattern expected = new TypeVariablePattern("T", new PatternParser("Number").parseTypePattern(),
				new TypePattern[] { new PatternParser("Comparable").parseTypePattern(),
						new PatternParser("Cloneable").parseTypePattern() }, null);
		assertEquals("Expected type variable T extends Number", expected, tv);
	}

	public void testParseTypeParameterList() {
		PatternParser parser = new PatternParser("<T>");
		TypeVariablePatternList list = parser.maybeParseTypeVariableList();
		TypeVariablePattern[] patterns = list.getTypeVariablePatterns();
		TypeVariablePattern expected = new TypeVariablePattern("T");
		assertEquals("Expected simple type variable T", expected, patterns[0]);
		assertEquals("One pattern in list", 1, patterns.length);
	}

	public void testParseTypeParameterListWithSeveralTypeParameters() {
		PatternParser parser = new PatternParser("<T,S extends Number, R>");
		TypeVariablePatternList list = parser.maybeParseTypeVariableList();
		TypeVariablePattern[] patterns = list.getTypeVariablePatterns();
		TypeVariablePattern expected0 = new TypeVariablePattern("T");
		assertEquals("Expected simple type variable T", expected0, patterns[0]);
		TypeVariablePattern expected1 = new TypeVariablePattern("S", new PatternParser("Number").parseTypePattern());
		assertEquals("Expected type variable S extends Number", expected1, patterns[1]);
		TypeVariablePattern expected2 = new TypeVariablePattern("R");
		assertEquals("Expected simple type variable R", expected2, patterns[2]);

		assertEquals("3 patterns in list", 3, patterns.length);
	}

	public void testParseAllowedSuperInTypeVariable() {
		PatternParser parser = new PatternParser("T super Number+");
		TypeVariablePattern tv = parser.parseTypeVariable();
		TypeVariablePattern expected = new TypeVariablePattern("T", new ExactTypePattern(UnresolvedType.OBJECT, false, false),
				null, new PatternParser("Number+").parseTypePattern());
		assertEquals("Expected type variable T super Number+", expected, tv);
	}

	public void testParseAnythingTypeVariable() {
		PatternParser parser = new PatternParser("?");
		WildTypePattern tp = (WildTypePattern) parser.parseTypePattern(true, false);
		assertEquals("Expected type variable ?", "?", tp.maybeGetSimpleName());
	}

	public void testParseAnythingExtendsTypeVariable() {
		PatternParser parser = new PatternParser("? extends Number");
		WildTypePattern tp = (WildTypePattern) parser.parseTypePattern(true, false);
		assertEquals("Expected type variable ?", "?", tp.maybeGetSimpleName());
		assertEquals("upper Bound of Number", new PatternParser("Number").parseTypePattern(), tp.getUpperBound());
	}

	public void testParseAnythingSuperTypeVariable() {
		PatternParser parser = new PatternParser("? super Number+");
		WildTypePattern tp = (WildTypePattern) parser.parseTypePattern(true, false);
		assertEquals("Expected type variable ?", "?", tp.maybeGetSimpleName());
		assertEquals("lower Bound of Number+", new PatternParser("Number+").parseTypePattern(), tp.getLowerBound());
	}

	public void testParseDeclareParentsWithTypeParameterList() {
		try {
			PatternParser parser = new PatternParser("declare parents<T> : Foo<T> implements IveGoneMad");
			// DeclareParents decp = (DeclareParents)
			parser.parseDeclare();
			// String[] tvp = decp.getTypeParameterNames();
			// assertEquals("one type parameter",1,tvp.length);
			// assertEquals("expecting T","T",tvp[0]);
			fail("Expecting parse exception");
		} catch (ParserException pEx) {
			assertEquals(":", pEx.getMessage());
		}
	}

	public void testParameterizedTypePatternsAny() {
		try {
			PatternParser parser = new PatternParser("*<T,S extends Number>");
			// WildTypePattern wtp = (WildTypePattern)
			parser.parseTypePattern(false, false);
			// TypePatternList tvs = wtp.getTypeParameters();
			// assertEquals("2 type parameters",2,tvs.getTypePatterns().length);
			// assertEquals("T",new PatternParser("T").parseTypePattern(),tvs.getTypePatterns()[0]);
			// assertEquals("S extends Number",new
			// PatternParser("S extends Number").parseTypePattern(false),tvs.getTypePatterns()[1]);
			fail("Expecting parse exception");
		} catch (ParserException pEx) {
			assertEquals(">", pEx.getMessage());
		}
	}

	public void testParameterizedTypePatternsSimple() {
		PatternParser parser = new PatternParser("List<String>");
		WildTypePattern wtp = (WildTypePattern) parser.parseTypePattern();
		TypePatternList tvs = wtp.getTypeParameters();
		assertEquals("1 type parameter", 1, tvs.getTypePatterns().length);
		assertEquals("String", new PatternParser("String").parseTypePattern(), tvs.getTypePatterns()[0]);
		assertEquals("List", wtp.getNamePatterns()[0].toString());
	}

	public void testNestedParameterizedTypePatterns() {
		PatternParser parser = new PatternParser("List<List<List<String>>>");
		WildTypePattern wtp = (WildTypePattern) parser.parseTypePattern();
		TypePatternList typeParameters = wtp.getTypeParameters();
		WildTypePattern expected = (WildTypePattern) typeParameters.getTypePatterns()[0];
		assertEquals("expecting a List", "List", expected.maybeGetSimpleName());
		typeParameters = expected.getTypeParameters();
		expected = (WildTypePattern) typeParameters.getTypePatterns()[0];
		assertEquals("expecting a List", "List", expected.maybeGetSimpleName());
		typeParameters = expected.getTypeParameters();
		expected = (WildTypePattern) typeParameters.getTypePatterns()[0];
		assertEquals("expecting a String", "String", expected.maybeGetSimpleName());
	}

	public void testSimpleTypeVariableList() {
		PatternParser parser = new PatternParser("<T,S,V>");
		String[] tl = parser.maybeParseSimpleTypeVariableList();
		assertEquals("3 patterns", 3, tl.length);
		assertEquals("T", tl[0]);
		assertEquals("S", tl[1]);
		assertEquals("V", tl[2]);
	}

	public void testSimpleTypeVariableListError() {
		PatternParser parser = new PatternParser("<T extends Number>");
		try {
			// String[] tl =
			parser.maybeParseSimpleTypeVariableList();
			fail();
		} catch (ParserException ex) {
			assertEquals("Expecting ',' or '>'", "',' or '>'", ex.getMessage());
		}
	}

	// test cases for pointcuts involving type variable specification.
	public void testParseCallPCDWithTypeVariables() {
		PatternParser parser = new PatternParser("call<T>(* Foo<T>.*(T))");
		try {
			parser.parsePointcut();
			// String[] tvps = pc.getTypeVariablesInScope();
			// assertEquals("1 type variable",1,tvps.length);
			// assertEquals("T",tvps[0]);
			fail("should have been a parse error");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testParseCallPCDWithIllegalBounds() {
		PatternParser parser = new PatternParser("call<T extends Number>(* Foo<T>.*(T))");
		try {
			parser.parsePointcut();
			fail("Expecting parse exception");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testNoTypeVarsForHandler() {
		PatternParser parser = new PatternParser("handler<T>(Exception<T>)");
		try {
			parser.parsePointcut();
			fail("Expecting parse exception");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testNoTypeVarsForThis() {
		PatternParser parser = new PatternParser("this<T>(Exception<T>)");
		try {
			parser.parsePointcut();
			fail("Expecting parse exception");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testNoTypeVarsForTarget() {
		PatternParser parser = new PatternParser("target<T>(Exception<T>)");
		try {
			parser.parsePointcut();
			fail("Expecting parse exception");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testNoTypeVarsForArgs() {
		PatternParser parser = new PatternParser("args<T>(Exception<T>)");
		try {
			parser.parsePointcut();
			fail("Expecting parse exception");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testNoTypeVarsForIf() {
		PatternParser parser = new PatternParser("if<T>(true)");
		try {
			parser.parsePointcut();
			fail("Expecting parse exception");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testNoTypeVarsForCflow() {
		PatternParser parser = new PatternParser("cflow<T>(call(* *(..)))");
		try {
			parser.parsePointcut();
			fail("Expecting parse exception");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testNoTypeVarsForCflowbelow() {
		PatternParser parser = new PatternParser("cflowbelow<T>(call(* *(..)))");
		try {
			parser.parsePointcut();
			fail("Expecting parse exception");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testNoTypeVarsForAtWithin() {
		PatternParser parser = new PatternParser("@within<T>(Foo<T>)");
		try {
			parser.parsePointcut();
			fail("Expecting parse exception");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testNoTypeVarsForAtAnnotation() {
		PatternParser parser = new PatternParser("@annotation<T>(Foo<T>)");
		try {
			parser.parsePointcut();
			fail("Expecting parse exception");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testNoTypeVarsForAtWithinCode() {
		PatternParser parser = new PatternParser("@withincode<T>(* Foo<T>.*(..))");
		try {
			parser.parsePointcut();
			fail("Expecting parse exception");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testNoTypeVarsForAtThis() {
		PatternParser parser = new PatternParser("@this<T>(Exception<T>)");
		try {
			parser.parsePointcut();
			fail("Expecting parse exception");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testNoTypeVarsForAtTarget() {
		PatternParser parser = new PatternParser("@target<T>(Exception<T>)");
		try {
			parser.parsePointcut();
			fail("Expecting parse exception");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testNoTypeVarsForAtArgs() {
		PatternParser parser = new PatternParser("@args<T>(Exception<T>)");
		try {
			parser.parsePointcut();
			fail("Expecting parse exception");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testExecutionWithTypeVariables() {
		PatternParser parser = new PatternParser("execution<T>(T Bar<T>.doSomething())");
		try {
			// Pointcut pc =
			parser.parsePointcut();
			// String[] tvs = pc.getTypeVariablesInScope();
			// assertEquals("1 type pattern",1,tvs.length);
			// assertEquals("T",tvs[0]);
			fail("should have been a parse error");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testInitializationWithTypeVariables() {
		PatternParser parser = new PatternParser("initialization<T>(Bar<T>.new())");
		try {
			// Pointcut pc =
			parser.parsePointcut();
			// String[] tvs = pc.getTypeVariablesInScope();
			// assertEquals("1 type pattern",1,tvs.length);
			// assertEquals("T",tvs[0]);
			fail("should have been a parse error");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testPreInitializationWithTypeVariables() {
		PatternParser parser = new PatternParser("preinitialization<T>(Bar<T>.new())");
		try {
			// Pointcut pc =
			parser.parsePointcut();
			// String[] tvs = pc.getTypeVariablesInScope();
			// assertEquals("1 type pattern",1,tvs.length);
			// assertEquals("T",tvs[0]);
			fail("should have been a parse error");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testStaticInitializationWithTypeVariables() {
		PatternParser parser = new PatternParser("staticinitialization<T>(Bar<T>)");
		try {
			parser.parsePointcut();
			// String[] tvs = pc.getTypeVariablesInScope();
			// assertEquals("1 type pattern",1,tvs.length);
			// assertEquals("T",tvs[0]);
			fail("should have been a parse error");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testWithinWithTypeVariables() {
		PatternParser parser = new PatternParser("within<T>(Bar<T>)");
		try {
			parser.parsePointcut();
			// String[] tvs = pc.getTypeVariablesInScope();
			// assertEquals("1 type pattern",1,tvs.length);
			// assertEquals("T",tvs[0]);
			fail("should have been a parse error");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testTypeParamList() {
		PatternParser parser = new PatternParser("Bar<T,S extends T, R extends S>");
		try {
			parser.parseTypePattern(false, false);
			// TypePattern[] tps = tp.getTypeParameters().getTypePatterns();
			// assertEquals("3 type patterns",3,tps.length);
			// assertEquals("T",tps[0].toString());
			// assertEquals("S",tps[1].toString());
			// assertEquals("R",tps[2].toString());
			fail("should have been a parse error");
		} catch (ParserException pEx) {
			assertEquals(">", pEx.getMessage());
		}
	}

	public void testWithinCodeWithTypeVariables() {
		PatternParser parser = new PatternParser("withincode<T,S,R>(Bar<T,S extends T, R extends S>.new())");
		try {
			parser.parsePointcut();
			// String[] tvs = pc.getTypeVariablesInScope();
			// assertEquals("3 type patterns",3,tvs.length);
			// assertEquals("T",tvs[0]);
			// assertEquals("S",tvs[1]);
			// assertEquals("R",tvs[2]);
			fail("should have been a parse error");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testCallWithTypeVariables() {
		PatternParser parser = new PatternParser("call<T>(* Bar<T>.*(..))");
		try {
			// Pointcut pc =
			parser.parsePointcut();
			// String[] tvs = pc.getTypeVariablesInScope();
			// assertEquals("1 type pattern",1,tvs.length);
			// assertEquals("T",tvs[0]);
			fail("should have been a parse error");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testGetWithTypeVariables() {
		PatternParser parser = new PatternParser("get<T>(* Bar<T>.*)");
		try {
			// Pointcut pc =
			parser.parsePointcut();
			// String[] tvs = pc.getTypeVariablesInScope();
			// assertEquals("1 type pattern",1,tvs.length);
			// assertEquals("T",tvs[0]);
			fail("should have been a parse error");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testSetWithTypeVariables() {
		PatternParser parser = new PatternParser("set<T>(* Bar<T>.*)");
		try {
			// Pointcut pc =
			parser.parsePointcut();
			// String[] tvs = pc.getTypeVariablesInScope();
			// assertEquals("1 type pattern",1,tvs.length);
			// assertEquals("T",tvs[0]);
			fail("should have been a parse error");
		} catch (ParserException pEx) {
			assertEquals("(", pEx.getMessage());
		}
	}

	public void testIntAnnotationVal() {
		PatternParser parser = new PatternParser("execution(@ComplexAnnotation(ival=5) * *(..))");
		Pointcut pc = parser.parsePointcut();
		assertEquals("Expected annotation value not found", "ival=5", getValueString(pc));
	}

	private String getValueString(Pointcut pc) {
		if (!(pc instanceof KindedPointcut)) {
			fail("Expected KindedPointcut but was " + pc.getClass());
		}
		KindedPointcut kpc = (KindedPointcut) pc;
		AnnotationTypePattern atp = kpc.getSignature().getAnnotationPattern();
		if (!(atp instanceof WildAnnotationTypePattern)) {
			fail("Expected WildAnnotationTypePattern but was " + atp.getClass());
		}
		WildAnnotationTypePattern watp = (WildAnnotationTypePattern) atp;
		Map<String,String> m = watp.annotationValues;
		Set<String> keys = m.keySet();
		List<String> orderedKeys = new ArrayList<>(keys);
		Collections.sort(orderedKeys);
		StringBuffer sb = new StringBuffer();
		for (Iterator<String> iterator = orderedKeys.iterator(); iterator.hasNext();) {
			String object = (String) iterator.next();
			sb.append(object).append("=").append(m.get(object));
			if (iterator.hasNext()) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	public void testByteAnnotationVal() {
		PatternParser parser = new PatternParser("execution(@ComplexAnnotation(bval=5) * *(..))");
		Pointcut pc = parser.parsePointcut();
		assertEquals("Expected annotation value not found", "bval=5", getValueString(pc));
	}

	public void testCharAnnotationVal() {
		PatternParser parser = new PatternParser("execution(@ComplexAnnotation(cval='5') * *(..))");
		Pointcut pc = parser.parsePointcut();
		assertEquals("Expected annotation value not found", "cval='5'", getValueString(pc));
	}

	public void testLongAnnotationVal() {
		PatternParser parser = new PatternParser("execution(@ComplexAnnotation(jval=123123) * *(..))");
		Pointcut pc = parser.parsePointcut();
		assertEquals("Expected annotation value not found", "jval=123123", getValueString(pc));
	}

	public void testDoubleAnnotationVal() {
		PatternParser parser = new PatternParser("execution(@ComplexAnnotation(dval=123.3) * *(..))");
		Pointcut pc = parser.parsePointcut();
		assertEquals("Expected annotation value not found", "dval=123.3", getValueString(pc));
	}

	public void testBooleanAnnotationVal() {
		PatternParser parser = new PatternParser("execution(@ComplexAnnotation(zval=true) * *(..))");
		Pointcut pc = parser.parsePointcut();
		assertEquals("Expected annotation value not found", "zval=true", getValueString(pc));
	}

	public void testShortAnnotationVal() {
		PatternParser parser = new PatternParser("execution(@ComplexAnnotation(sval=43) * *(..))");
		Pointcut pc = parser.parsePointcut();
		assertEquals("Expected annotation value not found", "sval=43", getValueString(pc));
	}

	public void testEnumAnnotationVal() {
		PatternParser parser = new PatternParser("execution(@ComplexAnnotation(enumval=Color.GREEN) * *(..))");
		Pointcut pc = parser.parsePointcut();
		assertEquals("Expected annotation value not found", "enumval=Color.GREEN", getValueString(pc));
	}

	public void testStringAnnotationVal() {
		PatternParser parser = new PatternParser("execution(@ComplexAnnotation(strval=\"abc\") * *(..))");
		Pointcut pc = parser.parsePointcut();
		// notice quotes stripped...
		assertEquals("Expected annotation value not found", "strval=abc", getValueString(pc));
	}

	public void testClassAnnotationVal() {
		PatternParser parser = new PatternParser("execution(@ComplexAnnotation(classval=String.class) * *(..))");
		Pointcut pc = parser.parsePointcut();
		assertEquals("Expected annotation value not found", "classval=String.class", getValueString(pc));
	}

	// failing as {1 is treated as a single token and so we don't realise the , is within the curlies
	// public void testArrayAnnotationVal() {
	// PatternParser parser = new PatternParser("execution(@ComplexAnnotation(arrayval={1,2,3}) * *(..))");
	// Pointcut pc = parser.parsePointcut();
	// assertEquals("Expected annotation value not found","arrayval={1,2,3}",getValueString(pc));
	// }

	// ---

	public TestScope makeSimpleScope() {
		world.setBehaveInJava5Way(true);
		TestScope s = new TestScope(new String[] { "int", "java.lang.String" }, new String[] { "a", "b" }, world);
		s.setImportedPrefixes(new String[] { "p." });
		return s;
	}

}
