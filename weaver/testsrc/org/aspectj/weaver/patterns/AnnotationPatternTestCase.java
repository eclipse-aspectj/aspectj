/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import org.aspectj.weaver.AnnotatedElement;
import org.aspectj.weaver.TypeX;

import junit.framework.TestCase;

public class AnnotationPatternTestCase extends TestCase {

	public void testParseSimpleAnnotationPattern() {
		PatternParser p = new PatternParser("@Foo");
		AnnotationTypePattern foo = p.parseAnnotationTypePattern();
		assertTrue("ExactAnnotationTypePattern",foo instanceof ExactAnnotationTypePattern);
		assertEquals("Foo",TypeX.forName("Foo"),((ExactAnnotationTypePattern)foo).annotationType);
	}
	
	public void testParseAndAnnotationPattern() {
		PatternParser p = new PatternParser("@Foo && @Goo");
		AnnotationTypePattern fooAndGoo = p.parseAnnotationTypePattern();
		assertTrue("AndAnnotationTypePattern",fooAndGoo instanceof AndAnnotationTypePattern);
		assertEquals("(@Foo && @Goo)",fooAndGoo.toString());
		AnnotationTypePattern left = ((AndAnnotationTypePattern)fooAndGoo).getLeft();
		AnnotationTypePattern right = ((AndAnnotationTypePattern)fooAndGoo).getRight();
		assertEquals("Foo",TypeX.forName("Foo"),((ExactAnnotationTypePattern)left).annotationType);
		assertEquals("Goo",TypeX.forName("Goo"),((ExactAnnotationTypePattern)right).annotationType);		
	}

	public void testParseOrAnnotationPattern() {
		PatternParser p = new PatternParser("@Foo || @Goo");
		AnnotationTypePattern fooOrGoo = p.parseAnnotationTypePattern();
		assertTrue("OrAnnotationTypePattern",fooOrGoo instanceof OrAnnotationTypePattern);
		assertEquals("(@Foo || @Goo)",fooOrGoo.toString());
		AnnotationTypePattern left = ((OrAnnotationTypePattern)fooOrGoo).getLeft();
		AnnotationTypePattern right = ((OrAnnotationTypePattern)fooOrGoo).getRight();
		assertEquals("Foo",TypeX.forName("Foo"),((ExactAnnotationTypePattern)left).annotationType);
		assertEquals("Goo",TypeX.forName("Goo"),((ExactAnnotationTypePattern)right).annotationType);		
	}
	
	public void testParseNotAnnotationPattern() {
		PatternParser p = new PatternParser("!@Foo");
		AnnotationTypePattern notFoo = p.parseAnnotationTypePattern();
		assertTrue("NotAnnotationTypePattern",notFoo instanceof NotAnnotationTypePattern);
		assertEquals("(!@Foo)",notFoo.toString());
		AnnotationTypePattern body = ((NotAnnotationTypePattern)notFoo).getNegatedPattern();
		assertEquals("Foo",TypeX.forName("Foo"),((ExactAnnotationTypePattern)body).annotationType);
	}

	public void testParseBracketedAnnotationPattern() {
		PatternParser p = new PatternParser("(@Foo)");
		AnnotationTypePattern foo = p.parseAnnotationTypePattern();
		assertTrue("ExactAnnotationTypePattern",foo instanceof ExactAnnotationTypePattern);
		assertEquals("Foo",TypeX.forName("Foo"),((ExactAnnotationTypePattern)foo).annotationType);		
	}
	
	public void testParseFQAnnPattern() {
		PatternParser p = new PatternParser("@org.aspectj.Foo");
		AnnotationTypePattern foo = p.parseAnnotationTypePattern();
		assertTrue("ExactAnnotationTypePattern",foo instanceof ExactAnnotationTypePattern);
		assertEquals("org.aspectj.Foo",TypeX.forName("org.aspectj.Foo"),((ExactAnnotationTypePattern)foo).annotationType);				
	}
	
	public void testParseComboPattern() {
		PatternParser p = new PatternParser("!((@Foo || @Goo) && !@Boo)");
		AnnotationTypePattern ap = p.parseAnnotationTypePattern();
		NotAnnotationTypePattern ntp = (NotAnnotationTypePattern) ap;
		AndAnnotationTypePattern atp = (AndAnnotationTypePattern) ntp.getNegatedPattern();
		NotAnnotationTypePattern notBoo = (NotAnnotationTypePattern) atp.getRight();
		ExactAnnotationTypePattern boo = (ExactAnnotationTypePattern) notBoo.getNegatedPattern();
		OrAnnotationTypePattern fooOrGoo = (OrAnnotationTypePattern) atp.getLeft();
		ExactAnnotationTypePattern foo = (ExactAnnotationTypePattern) fooOrGoo.getLeft();
		ExactAnnotationTypePattern goo = (ExactAnnotationTypePattern) fooOrGoo.getRight();
		assertEquals("(!((@Foo || @Goo) && (!@Boo)))",ap.toString());
	}
	
	public void testParseAndOrPattern() {
		PatternParser p = new PatternParser("@Foo && @Boo || @Goo");
		AnnotationTypePattern andOr = p.parseAnnotationTypePattern();
		assertTrue("Should be or pattern",andOr instanceof OrAnnotationTypePattern);
	}
	
	public void testParseBadPattern() {
		PatternParser p = new PatternParser("@@Foo");
		try {
			AnnotationTypePattern bad = p.parseAnnotationTypePattern();
			fail("ParserException expected");
		} catch(ParserException pEx) {
			assertEquals("identifier",pEx.getMessage());
		}
	}
	
	public void testParseBadPattern2() {
		PatternParser p = new PatternParser("Foo");
		try {
			AnnotationTypePattern bad = p.parseAnnotationTypePattern();
			fail("ParserException expected");
		} catch(ParserException pEx) {
			assertEquals("@",pEx.getMessage());
		}
	}
	public void testParseNameOrVarAnnotationPattern() {
		PatternParser p = new PatternParser("@Foo");
		AnnotationTypePattern foo = p.parseAnnotationNameOrVarTypePattern();
		assertTrue("ExactAnnotationTypePattern",foo instanceof ExactAnnotationTypePattern);
		assertEquals("Foo",TypeX.forName("Foo"),((ExactAnnotationTypePattern)foo).annotationType);		
	}
	
	public void testParseNameOrVarAnnotationPatternWithNot() {
		PatternParser p = new PatternParser("!@Foo");
		try {
			AnnotationTypePattern bad = p.parseAnnotationNameOrVarTypePattern();
			fail("ParserException expected");
		} catch(ParserException pEx) {
			assertEquals("identifier",pEx.getMessage());
		}		
	}

	public void testParseNameOrVarAnnotationPatternWithOr() {
		PatternParser p = new PatternParser("@Foo || @Boo");
		AnnotationTypePattern foo = p.parseAnnotationNameOrVarTypePattern();
		// rest of pattern not consumed...
		assertTrue("ExactAnnotationTypePattern",foo instanceof ExactAnnotationTypePattern);
		assertEquals("Foo",TypeX.forName("Foo"),((ExactAnnotationTypePattern)foo).annotationType);		
	}
	
	public void testParseNameOrVarAnnotationWithBinding() {
		PatternParser p = new PatternParser("foo");
		AnnotationTypePattern foo = p.parseAnnotationNameOrVarTypePattern();
		assertTrue("ExactAnnotationTypePattern",foo instanceof ExactAnnotationTypePattern);
		assertNull("no type pattern yet",((ExactAnnotationTypePattern)foo).annotationType);
		assertEquals("foo",((ExactAnnotationTypePattern)foo).formalName);
	}

	public void testParseNameOrVarAnnotationPatternWithAnd() {
		PatternParser p = new PatternParser("@Foo && @Boo");
		AnnotationTypePattern foo = p.parseAnnotationNameOrVarTypePattern();
		// rest of pattern not consumed...
		assertTrue("ExactAnnotationTypePattern",foo instanceof ExactAnnotationTypePattern);
		assertEquals("Foo",TypeX.forName("Foo"),((ExactAnnotationTypePattern)foo).annotationType);		
	}

	public void testMaybeParseAnnotationPattern() {
		PatternParser p = new PatternParser("@Foo && @Boo");
		AnnotationTypePattern a = p.maybeParseAnnotationPattern();
		assertNotNull("Should find annotation pattern",a);
		p = new PatternParser("Foo && Boo");
		a = p.maybeParseAnnotationPattern();
		assertEquals("Should be ANY pattern for a non-match",AnnotationTypePattern.ANY,a);
	}
	
	public void testParseTypePatternsWithAnnotations() {
		PatternParser p = new PatternParser("@Foo *");
		TypePattern t = p.parseTypePattern();
		assertTrue("WildTypePattern",t instanceof WildTypePattern);
		ExactAnnotationTypePattern etp = (ExactAnnotationTypePattern) t.annotationPattern;
		assertEquals("@Foo",etp.toString());
		assertEquals("@Foo *",t.toString());
	}
	
	public void testParseTypePatternsWithAnnotationsComplex() {
		PatternParser p = new PatternParser("(@(@Foo || @Boo) (Foo || Boo))");
		TypePattern t = p.parseTypePattern();
		assertTrue("OrTypePattern",t instanceof OrTypePattern);
		OrAnnotationTypePattern etp = (OrAnnotationTypePattern) t.annotationPattern;
		assertEquals("(@Foo || @Boo)",etp.toString());
		assertEquals("(@(@Foo || @Boo) (Foo || Boo))",t.toString());
	}
	
	public void testRidiculousNotSyntax() {
		PatternParser p = new PatternParser("(@(!@Foo) (Foo || Boo))");
		TypePattern t = p.parseTypePattern();
		assertTrue("OrTypePattern",t instanceof OrTypePattern);
		NotAnnotationTypePattern natp = (NotAnnotationTypePattern) t.annotationPattern;
		assertEquals("(!@Foo)",natp.toString());
		assertEquals("(@(!@Foo) (Foo || Boo))",t.toString());		
	}

	public void testParseMethodOrConstructorSigNoAP() {
		
	}
	
	public void testParseMethodOrConstructorSigSimpleAP() {
		
	}
	
	public void testParseMethodOrConstructorSigComplexAP() {
		
	}
	
	public void testParseMethodFieldSigNoAP() {
		
	}
	
	public void testParseFieldSigSimpleAP() {
		
	}
	
	public void testParseFieldSigComplexAP() {
		
	}
	
	public void testExactAnnotationPatternMatching() {
		// matching, non-matching
	}
	
	public void testBindingAnnotationPatternMatching() {
		// matching, non-matching
	}
	
	public void testAndAnnotationPatternMatching() {
		
	}
	
	public void testOrAnnotationPatternMatching() {
		
	}
	
	public void testNotAnnotationPatternMatching() {
		
	}
	
	public void testAnyAnnotationPatternMatching() {
		
	}
	
	// put test cases for AnnotationPatternList matching in separate test class...
	
	static class AnnotatedElementImpl implements AnnotatedElement {

		private boolean answer;
		
		public static final AnnotatedElementImpl YES = new AnnotatedElementImpl(true);
		public static final AnnotatedElementImpl NO = new AnnotatedElementImpl(false);
		
		public AnnotatedElementImpl(boolean answer) { this.answer = answer; }
		
		public boolean hasAnnotation(TypeX ofType) {
			return answer;
		}
		
	}
}
