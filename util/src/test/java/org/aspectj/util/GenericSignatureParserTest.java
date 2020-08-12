/* *******************************************************************
 * Copyright (c) 2005-2008 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * ******************************************************************/

package org.aspectj.util;

import org.aspectj.util.GenericSignature.ClassSignature;
import org.aspectj.util.GenericSignature.ClassTypeSignature;
import org.aspectj.util.GenericSignature.FieldTypeSignature;
import org.aspectj.util.GenericSignature.SimpleClassTypeSignature;

import junit.framework.TestCase;

/**
 * @author Adrian Colyer
 * @author Andy Clement
 */
public class GenericSignatureParserTest extends TestCase {

	GenericSignatureParser parser;

	public void testSimpleTokenize() {
		String[] tokens = parser.tokenize("Ljava/lang/String;");
		assertEquals(new String[] { "Ljava", "/", "lang", "/", "String", ";" }, tokens);
	}

	public void testTokenizeWithWildTypeArguments() {
		String[] tokens = parser.tokenize("Ljava/lang/String<*>;");
		assertEquals(new String[] { "Ljava", "/", "lang", "/", "String", "<", "*", ">", ";" }, tokens);
	}

	public void testTokenizeWithExtendsTypeArguments() {
		String[] tokens = parser.tokenize("Ljava/util/List<+TE>;");
		assertEquals(new String[] { "Ljava", "/", "util", "/", "List", "<", "+", "TE", ">", ";" }, tokens);
	}

	public void testTokenizeWithSuperTypeArguments() {
		String[] tokens = parser.tokenize("Ljava/util/List<-TE>;");
		assertEquals(new String[] { "Ljava", "/", "util", "/", "List", "<", "-", "TE", ">", ";" }, tokens);
	}

	public void testTokenizeArrayType() {
		String[] tokens = parser.tokenize("[Ljava/lang/String;");
		assertEquals(new String[] { "[", "Ljava", "/", "lang", "/", "String", ";" }, tokens);
	}

	public void testTokenizeFormalTypeParameters() {
		String[] tokens = parser.tokenize("<T:Ljava/lang/String;:Ljava/util/Comparable;>");
		assertEquals(new String[] { "<", "T", ":", "Ljava", "/", "lang", "/", "String", ";", ":", "Ljava", "/", "util", "/",
				"Comparable", ";", ">" }, tokens);
	}

	public void testParseClassSignatureSimple() {
		ClassSignature sig = parser.parseAsClassSignature("Ljava/lang/String;");
		assertEquals("No type parameters", 0, sig.formalTypeParameters.length);
		assertEquals("No superinterfaces", 0, sig.superInterfaceSignatures.length);
		assertEquals("Ljava/lang/String;", sig.superclassSignature.classSignature);
		SimpleClassTypeSignature outerType = sig.superclassSignature.outerType;
		assertEquals("Ljava/lang/String;", outerType.identifier);
		assertEquals("No type args", 0, outerType.typeArguments.length);
	}

	public void testParseClassSignatureTypeArgs() {
		ClassSignature sig = parser.parseAsClassSignature("Ljava/util/List<+Ljava/lang/String;>;");
		assertEquals("No type parameters", 0, sig.formalTypeParameters.length);
		assertEquals("No superinterfaces", 0, sig.superInterfaceSignatures.length);
		assertEquals("Ljava/util/List<+Ljava/lang/String;>;", sig.superclassSignature.classSignature);
		SimpleClassTypeSignature outerType = sig.superclassSignature.outerType;
		assertEquals("Ljava/util/List", outerType.identifier);
		assertEquals("One type arg", 1, outerType.typeArguments.length);
		assertTrue(outerType.typeArguments[0].isPlus);
		assertEquals("+Ljava/lang/String;", outerType.typeArguments[0].toString());
	}

	public void testParseClassSignatureTheFullMonty() {
		ClassSignature sig = parser
				.parseAsClassSignature("<E:Ljava/lang/String;:Ljava/lang/Number<TE;>;>Ljava/util/List<TE;>;Ljava/util/Comparable<-TE;>;");
		assertEquals("1 formal parameter", 1, sig.formalTypeParameters.length);
		assertEquals("E", sig.formalTypeParameters[0].identifier);
		ClassTypeSignature fsig = (ClassTypeSignature) sig.formalTypeParameters[0].classBound;
		assertEquals("Ljava/lang/String;", fsig.classSignature);
		assertEquals("1 interface bound", 1, sig.formalTypeParameters[0].interfaceBounds.length);
		ClassTypeSignature isig = (ClassTypeSignature) sig.formalTypeParameters[0].interfaceBounds[0];
		assertEquals("Ljava/lang/Number<TE;>;", isig.classSignature);
		assertEquals("Ljava/util/List<TE;>;", sig.superclassSignature.classSignature);
		assertEquals("1 type argument", 1, sig.superclassSignature.outerType.typeArguments.length);
		assertEquals("TE;", sig.superclassSignature.outerType.typeArguments[0].toString());
		assertEquals("1 super interface", 1, sig.superInterfaceSignatures.length);
		assertEquals("Ljava/util/Comparable<-TE;>;", sig.superInterfaceSignatures[0].toString());
	}

	public void testFunky_406167() {
		ClassSignature sig = parser
				.parseAsClassSignature("Lcom/google/android/gms/internal/hb<TT;>.com/google/android/gms/internal/hb$b<Ljava/lang/Boolean;>;");
		// Note the package prefix on the nested types has been dropped
		assertEquals("Lcom/google/android/gms/internal/hb<TT;>.hb$b<Ljava/lang/Boolean;>;",sig.superclassSignature.toString());
		sig = parser
				.parseAsClassSignature("Lcom/a/a/b/t<TK;TV;>.com/a/a/b/af.com/a/a/b/ag;Ljava/util/ListIterator<TV;>;");
		// Note the package prefix on the nested types has been dropped
		assertEquals("Lcom/a/a/b/t<TK;TV;>.af.ag;",sig.superclassSignature.toString());
		assertEquals("Ljava/util/ListIterator<TV;>;",sig.superInterfaceSignatures[0].toString());
		sig = parser.parseAsClassSignature("Lcom/google/android/gms/internal/hb.com/google/android/gms/internal/hb$b<Ljava/lang/Boolean;>;");
		// Note the package prefix on the nested types has been dropped
		assertEquals("Lcom/google/android/gms/internal/hb.hb$b<Ljava/lang/Boolean;>;",sig.superclassSignature.toString());
		sig = parser
				.parseAsClassSignature("Lcom/a/a/b/t.com/a/a/b/af.com/a/a/b/ag;Ljava/util/ListIterator<TV;>;");
		// Note the package prefix on the nested types has been dropped
		assertEquals("Lcom/a/a/b/t.af.ag;",sig.superclassSignature.toString());
		assertEquals("Ljava/util/ListIterator<TV;>;",sig.superInterfaceSignatures[0].toString());
	}


	public void testFieldSignatureParsingClassType() {
		FieldTypeSignature fsig = parser.parseAsFieldSignature("Ljava/lang/String;");
		assertTrue("ClassTypeSignature", fsig instanceof ClassTypeSignature);
		assertEquals("Ljava/lang/String;", fsig.toString());
	}

	public void testFieldSignatureParsingArrayType() {
		FieldTypeSignature fsig = parser.parseAsFieldSignature("[Ljava/lang/String;");
		assertTrue("ArrayTypeSignature", fsig instanceof GenericSignature.ArrayTypeSignature);
		assertEquals("[Ljava/lang/String;", fsig.toString());
	}

	public void testFieldSignatureParsingTypeVariable() {
		FieldTypeSignature fsig = parser.parseAsFieldSignature("TT;");
		assertTrue("TypeVariableSignature", fsig instanceof GenericSignature.TypeVariableSignature);
		assertEquals("TT;", fsig.toString());
	}

	public void testSimpleMethodSignatureParsing() {
		GenericSignature.MethodTypeSignature mSig = parser.parseAsMethodSignature("()V");
		assertEquals("No type parameters", 0, mSig.formalTypeParameters.length);
		assertEquals("No parameters", 0, mSig.parameters.length);
		assertEquals("Void return type", "V", mSig.returnType.toString());
		assertEquals("No throws", 0, mSig.throwsSignatures.length);
	}

	public void testMethodSignatureTypeParams() {
		GenericSignature.MethodTypeSignature mSig = parser.parseAsMethodSignature("<T:>(TT;)V");
		assertEquals("One type parameter", 1, mSig.formalTypeParameters.length);
		assertEquals("T", mSig.formalTypeParameters[0].identifier);
		assertEquals("Ljava/lang/Object;", mSig.formalTypeParameters[0].classBound.toString());
		assertEquals("One parameter", 1, mSig.parameters.length);
		assertEquals("TT;", mSig.parameters[0].toString());
		assertEquals("Void return type", "V", mSig.returnType.toString());
		assertEquals("No throws", 0, mSig.throwsSignatures.length);
	}

	public void testMethodSignatureGenericReturn() {
		GenericSignature.MethodTypeSignature mSig = parser.parseAsMethodSignature("<T:>()TT;");
		assertEquals("One type parameter", 1, mSig.formalTypeParameters.length);
		assertEquals("T", mSig.formalTypeParameters[0].identifier);
		assertEquals("Ljava/lang/Object;", mSig.formalTypeParameters[0].classBound.toString());
		assertEquals("No parameters", 0, mSig.parameters.length);
		assertEquals("'T' return type", "TT;", mSig.returnType.toString());
		assertEquals("No throws", 0, mSig.throwsSignatures.length);
	}

	public void testMethodSignatureThrows() {
		GenericSignature.MethodTypeSignature mSig = parser
				.parseAsMethodSignature("<T:>(TT;)V^Ljava/lang/Exception;^Ljava/lang/RuntimeException;");
		assertEquals("One type parameter", 1, mSig.formalTypeParameters.length);
		assertEquals("T", mSig.formalTypeParameters[0].identifier);
		assertEquals("Ljava/lang/Object;", mSig.formalTypeParameters[0].classBound.toString());
		assertEquals("One parameter", 1, mSig.parameters.length);
		assertEquals("TT;", mSig.parameters[0].toString());
		assertEquals("Void return type", "V", mSig.returnType.toString());
		assertEquals("2 throws", 2, mSig.throwsSignatures.length);
		assertEquals("Ljava/lang/Exception;", mSig.throwsSignatures[0].toString());
		assertEquals("Ljava/lang/RuntimeException;", mSig.throwsSignatures[1].toString());
	}

	public void testMethodSignaturePrimitiveParams() {
		GenericSignature.MethodTypeSignature mSig = parser.parseAsMethodSignature("(ILjava/lang/Object;)V");
		assertEquals("2 parameters", 2, mSig.parameters.length);
		assertEquals("I", mSig.parameters[0].toString());
		assertEquals("Ljava/lang/Object;", mSig.parameters[1].toString());
	}

	public void testFullyQualifiedSuperclassAfterTypeParams() {
		try {
			GenericSignature.FieldTypeSignature cSig = parser.parseAsFieldSignature("Ljava/util/List</;");
			fail("Expected IllegalStateException");
		} catch (IllegalStateException ex) {
			assertTrue(ex.getMessage().contains("Ljava/util/List</;"));
		}
	}

	public void testPr107784() {
		parser.parseAsMethodSignature("(Lcom/cibc/responders/mapping/CommonDataBeanScenario;Ljava/lang/Object;)Lcom/cibc/responders/response/Formatter<[BLjava/lang/Object;>;");
		parser.parseAsClassSignature("<Parent:Ljava/lang/Object;Child:Ljava/lang/Object;>Ljava/lang/Object;");
	}

	private void assertEquals(String[] expected, String[] actual) {
		if (actual.length != expected.length) {
			int shorter = Math.min(expected.length, actual.length);
			for (int i = 0; i < shorter; i++) {
				if (!actual[i].equals(expected[i])) {
					fail("Expected " + expected[i] + " at position " + i + " but found " + actual[i]);
				}
			}
			fail("Expected " + expected.length + " tokens but got " + actual.length + tokensToString(actual));
		}
		for (int i = 0; i < actual.length; i++) {
			if (!actual[i].equals(expected[i])) {
				fail("Expected " + expected[i] + " at position " + i + " but found " + actual[i]);
			}
		}
	}

	private String tokensToString(String[] tokens) {
		StringBuffer sb = new StringBuffer();
		sb.append(tokens[0]);
		for (int i = 1; i < tokens.length; i++) {
			sb.append(",");
			sb.append(tokens[i]);
		}
		return sb.toString();
	}

	protected void setUp() throws Exception {
		super.setUp();
		parser = new GenericSignatureParser();
	}
}
