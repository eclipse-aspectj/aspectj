/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.patterns;

import junit.framework.TestCase;

import org.aspectj.weaver.BcweaverTests;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelShadow;
import org.aspectj.weaver.bcel.BcelWorld;

import sun.reflect.generics.tree.TypeVariableSignature;

/**
 * @author hugunin
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ParserTestCase extends TestCase {

	public ParserTestCase(String arg0) {
		super(arg0);
	}
	
	World world = new BcelWorld(BcweaverTests.TESTDATA_PATH + "/testcode.jar");
	
	public void testNamePatterns() {
		
		
//		checkNoMatch("abc *", "abcd");
//		checkNoMatch("* d", "abcd");
	}
	
	
	
	public void testParse() {
		PatternParser parser = new PatternParser("execution(void Hello.*(..))");
		KindedPointcut p = (KindedPointcut) parser.parsePointcut();
		//System.out.println(p);
		assertEquals(p.kind, BcelShadow.MethodExecution);
		assertTrue(p.getSignature().getName().matches("foobar"));
		
		
		try {
			new PatternParser("initialization(void foo())").parsePointcut();
			fail("should have been a parse error");
		} catch (ParserException pe) {
			// good
		}
	}
	
	public void testParseExecutionWithAnnotation() {
		PatternParser parser = new PatternParser("execution(@SimpleAnnotation void Hello.*(..))");
		KindedPointcut p = (KindedPointcut) parser.parsePointcut();
		// XXX - needs finishing...
		p.resolveBindings(makeSimpleScope(),new Bindings(3));
		assertEquals("execution(@p.SimpleAnnotation void Hello.*(..))",p.toString());
		assertEquals(p.kind, Shadow.MethodExecution);
		assertTrue(p.getSignature().getName().matches("foobar"));
	}
	
	// note... toString on a pointcut is a very quick and easy way to test a successful parse
	public void testParseExecutionWithMultipleAnnotations() {
	    PatternParser parser = new PatternParser("execution(@SimpleAnnotation (@Foo Integer) (@Goo Hello).*(..))");
	    KindedPointcut p = (KindedPointcut) parser.parsePointcut();
	    assertEquals("execution(@(SimpleAnnotation) (@(Foo) Integer) (@(Goo) Hello).*(..))",p.toString());;
	}
	
	public void testParseCallWithMultipleAnnotations() {
	    PatternParser parser = new PatternParser("call(@SimpleAnnotation (@Foo Integer) (@Goo Hello).*(..))");
	    KindedPointcut p = (KindedPointcut) parser.parsePointcut();
	    assertEquals("call(@(SimpleAnnotation) (@(Foo) Integer) (@(Goo) Hello).*(..))",p.toString());;	    
	}
	
	public void testParseGetWithAnnotations() {
	    PatternParser parser = new PatternParser("get(@Foo (@SimpleAnnotation ReturnType) (@Foo @Goo Hello).*)");
	    KindedPointcut p = (KindedPointcut) parser.parsePointcut();
	    assertEquals("get(@(Foo) (@(SimpleAnnotation) ReturnType) (@(Foo) @(Goo) Hello).*)",p.toString());;	    
	}
	
	public void testParseBadGetWithAnnotations() {
	    PatternParser parser = new PatternParser("get(@Foo (@Foo @Goo Hello).*)");
	    try {
	        KindedPointcut p = (KindedPointcut) parser.parsePointcut();
	        fail("Expected parser exception");
	    } catch (ParserException pEx) {
	        assertEquals("name pattern",pEx.getMessage());
	    }
	}
	
	public void testParseGetWithAndAggregationAnnotations() {
	    PatternParser parser = new PatternParser("get(@Foo @SimpleAnnotation ReturnType (@Foo @Goo Hello).*)");
	    KindedPointcut p = (KindedPointcut) parser.parsePointcut();
	    assertEquals("get(@(Foo) @(SimpleAnnotation) ReturnType (@(Foo) @(Goo) Hello).*)",p.toString());;	    
	}
	
	
	public void testParseSetWithAnnotations() {
	    PatternParser parser = new PatternParser("set(@Foo (@SimpleAnnotation ReturnType) (@Foo @Goo Hello).*)");
	    KindedPointcut p = (KindedPointcut) parser.parsePointcut();
	    assertEquals("set(@(Foo) (@(SimpleAnnotation) ReturnType) (@(Foo) @(Goo) Hello).*)",p.toString());;	    
	}
	
	public void testParseHandlerWithAnnotations() {
	    PatternParser parser = new PatternParser("handler(@Critical Exception+)");
	    Pointcut p = parser.parsePointcut();
	    assertEquals("handler((@(Critical) Exception+))",p.toString());;	    
	}

	public void testParseInitializationWithAnnotations() {
	    PatternParser parser = new PatternParser("initialization(@Foo (@Goo Hello).new(@Foo Integer))");
	    Pointcut p = parser.parsePointcut();
	    assertEquals("initialization(@(Foo) (@(Goo) Hello).new((@(Foo) Integer)))",p.toString());	    
	    
	}
	
	public void testParsePreInitializationWithAnnotations() {
	    PatternParser parser = new PatternParser("preinitialization(@Foo (@Goo Hello).new(@Foo Integer))");
	    Pointcut p = parser.parsePointcut();
	    assertEquals("preinitialization(@(Foo) (@(Goo) Hello).new((@(Foo) Integer)))",p.toString());	    	    
	}
	
	public void testStaticInitializationWithAnnotations() {
	    PatternParser parser = new PatternParser("staticinitialization(@Foo @Boo @Goo Moo)");
	    Pointcut p = parser.parsePointcut();
	    assertEquals("staticinitialization((@(Foo) @(Boo) @(Goo) Moo).<clinit>())",p.toString());	    	    	    
	}
	
	public void testWithinWithAnnotations() {
	    PatternParser parser = new PatternParser("within(@Foo *)");
	    Pointcut p = parser.parsePointcut();
	    assertEquals("within((@(Foo) *))",p.toString());	    	    	    	    
	}
	
	public void testWithinCodeWithAnnotations() {
	    PatternParser parser = new PatternParser("withincode(@Foo * *.*(..))");
	    Pointcut p = parser.parsePointcut();
	    assertEquals("withincode(@(Foo) * *.*(..))",p.toString());	    	    	    	    	    
	}
	
	public void testAtAnnotation() {
	    PatternParser parser = new PatternParser("@annotation(Foo)");
	    AnnotationPointcut p = (AnnotationPointcut) parser.parsePointcut();
	    assertEquals("@annotation(Foo)",p.toString());	    	    	    	    	    	    
	}
	
	public void testBadAtAnnotation() {
	    PatternParser parser = new PatternParser("@annotation(!Foo)");
	    try {
	        Pointcut p = parser.parsePointcut();
	        fail("Expected parser exception");
	    } catch (ParserException pEx) {
	        assertEquals("identifier",pEx.getMessage());
	    }	    
	}
	
	public void testAtAnnotationWithBinding() {
	    PatternParser parser = new PatternParser("@annotation(foo)");
	    AnnotationPointcut p = (AnnotationPointcut) parser.parsePointcut();
	    assertEquals("@annotation(foo)",p.toString());	    	    	    	    	    	    	    
	}
	
	public void testDoubleAtAnnotation() {
	    PatternParser parser = new PatternParser("@annotation(Foo Goo)");
	    try {
	        Pointcut p = parser.parsePointcut();
	        fail("Expected parser exception");
	    } catch (ParserException pEx) {
	        assertEquals(")",pEx.getMessage());
	    }	    	    
	}
	
	public void testAtWithin() {
	    PatternParser parser = new PatternParser("@within(foo)");
	    WithinAnnotationPointcut p = (WithinAnnotationPointcut) parser.parsePointcut();
	    assertEquals("@within(foo)",p.toString());	 
	    parser = new PatternParser("@within(Foo))");
	    p = (WithinAnnotationPointcut) parser.parsePointcut();
	    assertEquals("@within(Foo)",p.toString());
	}
	
	public void testAtWithinCode() {
	    PatternParser parser = new PatternParser("@withincode(foo)");
	    WithinCodeAnnotationPointcut p = (WithinCodeAnnotationPointcut) parser.parsePointcut();
	    assertEquals("@withincode(foo)",p.toString());	 
	    parser = new PatternParser("@withincode(Foo))");
	    p = (WithinCodeAnnotationPointcut) parser.parsePointcut();
	    assertEquals("@withincode(Foo)",p.toString());
	}
	
	public void testAtThis() {
	    PatternParser parser = new PatternParser("@this(foo)");
	    ThisOrTargetAnnotationPointcut p = (ThisOrTargetAnnotationPointcut) parser.parsePointcut();
	    assertEquals("@this(foo)",p.toString());
	    assertTrue("isThis",p.isThis());
	    parser = new PatternParser("@this(Foo))");
	    p = (ThisOrTargetAnnotationPointcut) parser.parsePointcut();
	    assertTrue("isThis",p.isThis());
	    assertEquals("@this(Foo)",p.toString());
	}

	public void testAtTarget() {
	    PatternParser parser = new PatternParser("@target(foo)");
	    ThisOrTargetAnnotationPointcut p = (ThisOrTargetAnnotationPointcut) parser.parsePointcut();
	    assertEquals("@target(foo)",p.toString());
	    assertTrue("isTarget",!p.isThis());
	    parser = new PatternParser("@target(Foo))");
	    p = (ThisOrTargetAnnotationPointcut) parser.parsePointcut();
	    assertTrue("isTarget",!p.isThis());
	    assertEquals("@target(Foo)",p.toString());
	}
	
	public void testAtArgs() {
	    PatternParser parser = new PatternParser("@args(Foo,Goo,*,..,Moo)");
	    Pointcut p = parser.parsePointcut();
	    assertEquals("@args(Foo, Goo, ANY, .., Moo)",p.toString());
	}
	
	public void testParseSimpleTypeVariable() {
		PatternParser parser = new PatternParser("T");
		TypeVariable tv = parser.parseTypeVariable();
		TypeVariable expected = new TypeVariable("T");
		assertEquals("Expected simple type variable T",expected,tv);
	}
	
	public void testParseExtendingTypeVariable() {
		PatternParser parser = new PatternParser("T extends Number");
		TypeVariable tv = parser.parseTypeVariable();
		TypeVariable expected = new TypeVariable("T",new PatternParser("Number").parseTypePattern());
		assertEquals("Expected type variable T extends Number",expected,tv);
	}
	
	public void testParseExtendingTypeVariableWithPattern() {
		PatternParser parser = new PatternParser("T extends Number+");
		TypeVariable tv = parser.parseTypeVariable();
		TypeVariable expected = new TypeVariable("T",new PatternParser("Number+").parseTypePattern());
		assertEquals("Expected type variable T extends Number+",expected,tv);		
	}
	
	public void testParseExtendingTypeVariableWithInterface() {
		PatternParser parser = new PatternParser("T extends Number & Comparable");
		TypeVariable tv = parser.parseTypeVariable();
		TypeVariable expected = new TypeVariable("T",new PatternParser("Number").parseTypePattern(),
				new TypePattern[] {new PatternParser("Comparable").parseTypePattern()},null);
		assertEquals("Expected type variable T extends Number",expected,tv);
	}
	
	public void testParseExtendingTypeVariableWithInterfaceList() {
		PatternParser parser = new PatternParser("T extends Number & Comparable & Cloneable");
		TypeVariable tv = parser.parseTypeVariable();
		TypeVariable expected = new TypeVariable("T",new PatternParser("Number").parseTypePattern(),
				new TypePattern[] {new PatternParser("Comparable").parseTypePattern(),
			                                new PatternParser("Cloneable").parseTypePattern()},null);
		assertEquals("Expected type variable T extends Number",expected,tv);
	}
	
	public void testParseTypeParameterList() {
		PatternParser parser = new PatternParser("<T>");
		TypeVariablePatternList list = parser.maybeParseTypeVariableList();
		TypeVariable[] patterns = list.getTypeVariablePatterns();
		TypeVariable expected = new TypeVariable("T");
		assertEquals("Expected simple type variable T",expected,patterns[0]);
		assertEquals("One pattern in list",1,patterns.length);
	}
	
	public void testParseTypeParameterListWithSeveralTypeParameters() {
		PatternParser parser = new PatternParser("<T,S extends Number, R>");
		TypeVariablePatternList list = parser.maybeParseTypeVariableList();
		TypeVariable[] patterns = list.getTypeVariablePatterns();
		TypeVariable expected0 = new TypeVariable("T");
		assertEquals("Expected simple type variable T",expected0,patterns[0]);
		TypeVariable expected1 = new TypeVariable("S",new PatternParser("Number").parseTypePattern());
		assertEquals("Expected type variable S extends Number",expected1,patterns[1]);
		TypeVariable expected2 = new TypeVariable("R");
		assertEquals("Expected simple type variable R",expected2,patterns[2]);
		
		assertEquals("3 patterns in list",3,patterns.length);
	}
	
	
	public void testParseAllowedSuperInTypeVariable() {
		PatternParser parser = new PatternParser("T super Number+");
		TypeVariable tv = parser.parseTypeVariable();
		TypeVariable expected = new TypeVariable("T",new ExactTypePattern(TypeX.OBJECT,false,false),null,new PatternParser("Number+").parseTypePattern());
		assertEquals("Expected type variable T super Number+",expected,tv);				
	}
	
	public void testParseAnythingTypeVariable() {
		PatternParser parser = new PatternParser("?");
		WildTypePattern tp = (WildTypePattern) parser.parseTypePattern(true,false);
		assertEquals("Expected type variable ?","?",tp.maybeGetSimpleName());
	}

	public void testParseAnythingExtendsTypeVariable() {
		PatternParser parser = new PatternParser("? extends Number");
		WildTypePattern tp = (WildTypePattern) parser.parseTypePattern(true,false);
		assertEquals("Expected type variable ?","?",tp.maybeGetSimpleName());
		assertEquals("upper Bound of Number",new PatternParser("Number").parseTypePattern(),tp.getUpperBound());
	}
	
	public void testParseAnythingSuperTypeVariable() {
		PatternParser parser = new PatternParser("? super Number+");
		WildTypePattern tp = (WildTypePattern) parser.parseTypePattern(true,false);
		assertEquals("Expected type variable ?","?",tp.maybeGetSimpleName());
		assertEquals("lower Bound of Number+",new PatternParser("Number+").parseTypePattern(),tp.getLowerBound());
	}
	
	public void testParseDeclareParentsWithTypeParameterList() {
		PatternParser parser = new PatternParser("declare parents : <T> Foo<T> implements IveGoneMad");
		DeclareParents decp = (DeclareParents) parser.parseDeclare();
		TypeVariablePatternList tvp = decp.getTypeParameters();
		assertEquals("one type parameter",1,tvp.getTypeVariablePatterns().length);
		assertEquals("expecting T","T",tvp.getTypeVariablePatterns()[0].getName());
	}
	
	public void testParameterizedTypePatternsAny() {
		PatternParser parser = new PatternParser("*<T,S extends Number>");
		WildTypePattern wtp = (WildTypePattern) parser.parseTypePattern(false,true);
		TypePatternList tvs = wtp.getTypeParameters();
		assertEquals("2 type parameters",2,tvs.getTypePatterns().length);
		assertEquals("T",new PatternParser("T").parseTypePattern(),tvs.getTypePatterns()[0]);
		assertEquals("S extends Number",new PatternParser("S extends Number").parseTypePattern(false,true),tvs.getTypePatterns()[1]);
	}
	
	public void testParameterizedTypePatternsSimple() {
		PatternParser parser = new PatternParser("List<String>");
		WildTypePattern wtp = (WildTypePattern) parser.parseTypePattern();
		TypePatternList tvs = wtp.getTypeParameters();
		assertEquals("1 type parameter",1,tvs.getTypePatterns().length);		
		assertEquals("String",new PatternParser("String").parseTypePattern(),tvs.getTypePatterns()[0]);
		assertEquals("List",wtp.getNamePatterns()[0].toString());
	}
	
	public void testNestedParameterizedTypePatterns() {
		PatternParser parser = new PatternParser("List<List<List<String>>>");
		WildTypePattern wtp = (WildTypePattern) parser.parseTypePattern();
		TypePatternList typeParameters = wtp.getTypeParameters();
		WildTypePattern expected = (WildTypePattern) typeParameters.getTypePatterns()[0];
		assertEquals("expecting a List", "List",expected.maybeGetSimpleName());
		typeParameters = expected.getTypeParameters();
		expected = (WildTypePattern) typeParameters.getTypePatterns()[0];
		assertEquals("expecting a List", "List",expected.maybeGetSimpleName());
		typeParameters = expected.getTypeParameters();
		expected = (WildTypePattern) typeParameters.getTypePatterns()[0];
		assertEquals("expecting a String", "String",expected.maybeGetSimpleName());
	}

	public void testSimpleTypeVariableList() {
		PatternParser parser = new PatternParser("<T,S,V>");
		TypeVariablePatternList tl = parser.maybeParseSimpleTypeVariableList();
		TypeVariable[] patterns = tl.getTypeVariablePatterns();
		assertEquals("3 patterns",3,patterns.length);
		assertEquals("T",new TypeVariable("T"),patterns[0]);
		assertEquals("S",new TypeVariable("S"),patterns[1]);
		assertEquals("V",new TypeVariable("V"),patterns[2]);
	}
	
	public void testSimpleTypeVariableListError() {
		PatternParser parser = new PatternParser("<T extends Number>");
		try {
			TypeVariablePatternList tl = parser.maybeParseSimpleTypeVariableList();
		} catch (ParserException ex) {
			assertEquals("Expecting >",">",ex.getMessage());
		}	
	}
	
	public TestScope makeSimpleScope() {
		TestScope s = new TestScope(new String[] {"int", "java.lang.String"}, new String[] {"a", "b"}, world);
		s.setImportedPrefixes(new String[]{"p."});
		return s;
	}

}


