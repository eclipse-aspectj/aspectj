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
 * ******************************************************************/

package org.aspectj.weaver.patterns;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.TestUtils;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;
import org.aspectj.weaver.reflect.ReflectionWorld;

public class SignaturePatternTestCase extends PatternsTestCase {

	@Override
	public World getWorld() {
		return new ReflectionWorld(true, this.getClass().getClassLoader());
	}

	public void testThrowsMatch() throws IOException {
		Member onlyDerivedOnDerived = TestUtils.methodFromString("static void org.aspectj.weaver.testcode.Derived.onlyDerived()");
		Member mOnBase = TestUtils.methodFromString("void org.aspectj.weaver.testcode.Base.m()");
		Member mOnDerived = TestUtils.methodFromString("void org.aspectj.weaver.testcode.Derived.m()");

		checkMatch(makeMethodPat("* org.aspectj.weaver.testcode.Base.*(..) throws java.lang.CloneNotSupportedException"),
				new Member[] { mOnBase }, new Member[] { mOnDerived });

		checkMatch(makeMethodPat("* org.aspectj.weaver.testcode.Derived.*(..) throws java.lang.CloneNotSupportedException"),
				new Member[] {}, new Member[] { mOnBase, mOnDerived });

		// XXX need pattern checks
		Member[] NONE = new Member[] {};
		Member[] M = new Member[] { onlyDerivedOnDerived };
		Member[] NO_EXCEPTIONS = new Member[] { mOnDerived };
		Member[] BOTH = new Member[] { mOnDerived, onlyDerivedOnDerived };

		checkMatch(makeMethodPat("* *(..)"), M, NONE);
		checkMatch(makeMethodPat("* *(..) throws !*"), NO_EXCEPTIONS, M);
		checkMatch(makeMethodPat("* *(..) throws *"), M, NO_EXCEPTIONS);
		checkMatch(makeMethodPat("* *(..) throws *, !*"), NONE, BOTH);

		checkMatch(makeMethodPat("* *(..) throws (!*)"), NONE, BOTH);
		checkMatch(makeMethodPat("* *(..) throws !(!*)"), BOTH, NONE);

		checkMatch(makeMethodPat("* *(..) throws *..IOException"), M, NO_EXCEPTIONS);
		checkMatch(makeMethodPat("* *(..) throws *..IOException, *..Clone*"), M, NO_EXCEPTIONS);
		checkMatch(makeMethodPat("* *(..) throws *..IOException, !*..Clone*"), NONE, BOTH);
		checkMatch(makeMethodPat("* *(..) throws !*..IOException"), NO_EXCEPTIONS, M);
	}

	/*
	 * public void testInstanceMethodMatchSpeed() throws IOException { // Member objectToString =
	 * TestUtils.methodFromString("java.lang.String java.lang.Object.toString()"); Member objectToString =
	 * TestUtils.methodFromString(
	 * "java.lang.String java.lang.String.replaceFirst(java.lang.String,java.lang.String)").resolve(world); SignaturePattern
	 * signaturePattern = makeMethodPat("* *(..))"); signaturePattern = signaturePattern.resolveBindings(new TestScope(world, new
	 * FormalBinding[0]), new Bindings(0)); for (int i = 0; i < 1000; i++) { boolean matches =
	 * signaturePattern.matches(objectToString, world, false); } long stime = System.currentTimeMillis(); for (int i = 0; i <
	 * 2000000; i++) { boolean matches = signaturePattern.matches(objectToString, world, false); } long etime =
	 * System.currentTimeMillis(); System.out.println("Took " + (etime - stime) + "ms for 2,000,000");// 4081
	 * 
	 * signaturePattern = makeMethodPat("* *())"); signaturePattern = signaturePattern.resolveBindings(new TestScope(world, new
	 * FormalBinding[0]), new Bindings(0)); for (int i = 0; i < 1000; i++) { boolean matches =
	 * signaturePattern.matches(objectToString, world, false); } stime = System.currentTimeMillis(); for (int i = 0; i < 2000000;
	 * i++) { boolean matches = signaturePattern.matches(objectToString, world, false); } etime = System.currentTimeMillis();
	 * System.out.println("Took " + (etime - stime) + "ms for 2,000,000");// 4081 }
	 * 
	 * public void testInstanceMethodMatchSpeed2() throws IOException { // Member objectToString =
	 * TestUtils.methodFromString("java.lang.String java.lang.Object.toString()"); Member objectToString =
	 * TestUtils.methodFromString(
	 * "java.lang.String java.lang.String.replaceFirst(java.lang.String,java.lang.String)").resolve(world); SignaturePattern
	 * signaturePattern = makeMethodPat("!void *(..))"); signaturePattern = signaturePattern.resolveBindings(new TestScope(world,
	 * new FormalBinding[0]), new Bindings(0)); for (int i = 0; i < 1000; i++) { boolean matches =
	 * signaturePattern.matches(objectToString, world, false); } long stime = System.currentTimeMillis(); for (int i = 0; i <
	 * 2000000; i++) { boolean matches = signaturePattern.matches(objectToString, world, false); } long etime =
	 * System.currentTimeMillis(); System.out.println("Took " + (etime - stime) + "ms for 2,000,000");// 4081 }
	 */
	public void testInstanceMethodMatch() throws IOException {
		Member objectToString = TestUtils.methodFromString("java.lang.String java.lang.Object.toString()");
		Member integerToString = TestUtils.methodFromString("java.lang.String java.lang.Integer.toString()");
		Member integerIntValue = TestUtils.methodFromString("int java.lang.Integer.intValue()");
		// Member objectToString = Member.methodFromString("java.lang.String java.lang.Object.toString()");

		checkMatch(makeMethodPat("* java.lang.Object.*(..)"), new Member[] { objectToString, integerToString },
				new Member[] { integerIntValue });

		checkMatch(makeMethodPat("* java.lang.Integer.*(..)"), new Member[] { integerIntValue, integerToString },
				new Member[] { objectToString });
	}

	public void testStaticMethodMatch() throws IOException {
		Member onlyBaseOnBase = TestUtils.methodFromString("static void org.aspectj.weaver.testcode.Base.onlyBase()");
		Member onlyBaseOnDerived = TestUtils.methodFromString("static void org.aspectj.weaver.testcode.Derived.onlyBase()");
		Member onlyDerivedOnDerived = TestUtils.methodFromString("static void org.aspectj.weaver.testcode.Derived.onlyDerived()");
		Member bothOnBase = TestUtils.methodFromString("static void org.aspectj.weaver.testcode.Base.both()");
		Member bothOnDerived = TestUtils.methodFromString("static void org.aspectj.weaver.testcode.Derived.both()");

		checkMatch(makeMethodPat("* org.aspectj.weaver.testcode.Base.*(..)"), new Member[] { onlyBaseOnBase, onlyBaseOnDerived,
				bothOnBase }, new Member[] { onlyDerivedOnDerived, bothOnDerived });

		checkMatch(makeMethodPat("* org.aspectj.weaver.testcode.Derived.*(..)"), new Member[] { onlyBaseOnDerived, bothOnDerived,
				onlyDerivedOnDerived }, new Member[] { onlyBaseOnBase, bothOnBase });
	}

	public void testFieldMatch() throws IOException {
		Member onlyBaseOnBase = TestUtils.fieldFromString("int org.aspectj.weaver.testcode.Base.onlyBase");
		Member onlyBaseOnDerived = TestUtils.fieldFromString("int org.aspectj.weaver.testcode.Derived.onlyBase");
		Member onlyDerivedOnDerived = TestUtils.fieldFromString("int org.aspectj.weaver.testcode.Derived.onlyDerived");
		Member bothOnBase = TestUtils.fieldFromString("int org.aspectj.weaver.testcode.Base.both");
		Member bothOnDerived = TestUtils.fieldFromString("int org.aspectj.weaver.testcode.Derived.both");

		checkMatch(makeFieldPat("* org.aspectj.weaver.testcode.Base.*"), new Member[] { onlyBaseOnBase, onlyBaseOnDerived,
				bothOnBase }, new Member[] { onlyDerivedOnDerived, bothOnDerived });

		checkMatch(makeFieldPat("* org.aspectj.weaver.testcode.Derived.*"), new Member[] { onlyBaseOnDerived, bothOnDerived,
				onlyDerivedOnDerived }, new Member[] { onlyBaseOnBase, bothOnBase });
	}

	public void testConstructorMatch() throws IOException {
		Member onBase = TestUtils.methodFromString("void org.aspectj.weaver.testcode.Base.<init>()");
		Member onDerived = TestUtils.methodFromString("void org.aspectj.weaver.testcode.Derived.<init>()");
		Member onBaseWithInt = TestUtils.methodFromString("void org.aspectj.weaver.testcode.Base.<init>(int)");

		checkMatch(makeMethodPat("org.aspectj.weaver.testcode.Base.new(..)"), new Member[] { onBase, onBaseWithInt },
				new Member[] { onDerived });

		checkMatch(makeMethodPat("org.aspectj.weaver.testcode.Derived.new(..)"), new Member[] { onDerived }, new Member[] { onBase,
				onBaseWithInt });
	}

	public void checkMatch(SignaturePattern p, Member[] yes, Member[] no) throws IOException {
		p = p.resolveBindings(new TestScope(world, new FormalBinding[0]), new Bindings(0));

		for (Member value : yes) {
			checkMatch(p, value, true);
		}

		for (Member member : no) {
			checkMatch(p, member, false);
		}

		checkSerialization(p);
	}

	private void checkMatch(SignaturePattern p, Member member, boolean b) {
		boolean matches = p.matches(member, world, false);
		assertEquals(p.toString() + " matches " + member.toString(), b, matches);
	}

	private SignaturePattern makeMethodPat(String pattern) {
		return new PatternParser(pattern).parseMethodOrConstructorSignaturePattern();
	}

	private SignaturePattern makeFieldPat(String pattern) {
		return new PatternParser(pattern).parseFieldSignaturePattern();
	}

	private void checkSerialization(SignaturePattern p) throws IOException {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ConstantPoolSimulator cps = new ConstantPoolSimulator();
		CompressingDataOutputStream out = new CompressingDataOutputStream(bo, cps);
		p.write(out);
		out.close();

		ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
		VersionedDataInputStream in = new VersionedDataInputStream(bi, cps);
		SignaturePattern newP = SignaturePattern.read(in, null);

		assertEquals("write/read", p, newP);
	}

}
