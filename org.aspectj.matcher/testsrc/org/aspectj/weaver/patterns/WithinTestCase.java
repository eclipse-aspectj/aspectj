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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.TestShadow;
import org.aspectj.weaver.TestUtils;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;
import org.aspectj.weaver.reflect.ReflectionWorld;

public class WithinTestCase extends PatternsTestCase {

	public World getWorld() {
		return new ReflectionWorld(true, this.getClass().getClassLoader());
	}

	public void testMatch() throws IOException {
		Shadow getOutFromArrayList = new TestShadow(Shadow.FieldGet, TestUtils
				.fieldFromString("java.io.PrintStream java.lang.System.out"), UnresolvedType.forName("java.util.ArrayList"), world);

		checkMatch(makePointcut("within(*)"), getOutFromArrayList, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.util.*)"), getOutFromArrayList, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.lang.*)"), getOutFromArrayList, FuzzyBoolean.NO);
		checkMatch(makePointcut("within(java.util.List+)"), getOutFromArrayList, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.uti*.List+)"), getOutFromArrayList, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.uti*..*)"), getOutFromArrayList, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.util.*List)"), getOutFromArrayList, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.util.List*)"), getOutFromArrayList, FuzzyBoolean.NO);

		Shadow getOutFromEntry = new TestShadow(Shadow.FieldGet, TestUtils
				.fieldFromString("java.io.PrintStream java.lang.System.out"), UnresolvedType.forName("java.util.Map$Entry"), world);

		checkMatch(makePointcut("within(*)"), getOutFromEntry, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.util.*)"), getOutFromEntry, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.util.Map.*)"), getOutFromEntry, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.util..*)"), getOutFromEntry, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.util.Map..*)"), getOutFromEntry, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.lang.*)"), getOutFromEntry, FuzzyBoolean.NO);
		checkMatch(makePointcut("within(java.util.List+)"), getOutFromEntry, FuzzyBoolean.NO);
		checkMatch(makePointcut("within(java.util.Map+)"), getOutFromEntry, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.lang.Object+)"), getOutFromEntry, FuzzyBoolean.YES);

		// this is something we should in type patterns tests
		// checkMatch(makePointcut("within(*List)"), getOut, FuzzyBoolean.NO);

	}

	// public void testMatchJP() {
	// Factory f = new Factory("WithinTestCase.java",WithinTestCase.class);
	//		
	// JoinPoint.StaticPart inString = f.makeSJP(JoinPoint.CONSTRUCTOR_EXECUTION,f.makeConstructorSig(0,String.class,new Class[]
	// {String.class},new String[]{"s"},new Class[0]),1);
	// JoinPoint.StaticPart inObject = f.makeSJP(JoinPoint.CONSTRUCTOR_EXECUTION,f.makeConstructorSig(0,Object.class,new Class[]
	// {},new String[]{},new Class[0]),1);
	//
	// Pointcut withinString = new PatternParser("within(String)").parsePointcut().resolve();
	// Pointcut withinObject = new PatternParser("within(Object)").parsePointcut().resolve();
	// Pointcut withinObjectPlus = new PatternParser("within(Object+)").parsePointcut().resolve();
	//		
	// checkMatches(withinString,inString,FuzzyBoolean.YES);
	// checkMatches(withinString,inObject,FuzzyBoolean.NO);
	// checkMatches(withinObject,inString,FuzzyBoolean.NO);
	// checkMatches(withinObject,inObject, FuzzyBoolean.YES);
	// checkMatches(withinObjectPlus,inString,FuzzyBoolean.YES);
	// checkMatches(withinObjectPlus,inObject,FuzzyBoolean.YES);
	// }
	//	
	// private void checkMatches(Pointcut p, JoinPoint.StaticPart jpsp, FuzzyBoolean expected) {
	// assertEquals(expected,p.match(null,jpsp));
	// }
	//	
	public Pointcut makePointcut(String pattern) {
		Pointcut pointcut0 = Pointcut.fromString(pattern);

		Bindings bindingTable = new Bindings(0);
		IScope scope = new SimpleScope(world, FormalBinding.NONE);

		pointcut0.resolveBindings(scope, bindingTable);
		Pointcut pointcut1 = pointcut0;
		return pointcut1.concretize1(null, null, new IntMap());
	}

	private void checkMatch(Pointcut p, Shadow s, FuzzyBoolean shouldMatch) throws IOException {
		FuzzyBoolean doesMatch = p.match(s);
		assertEquals(p + " matches " + s, shouldMatch, doesMatch);
		checkSerialization(p);
	}

	private void checkSerialization(Pointcut p) throws IOException {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ConstantPoolSimulator cps = new ConstantPoolSimulator();
		CompressingDataOutputStream out = new CompressingDataOutputStream(bo, cps);
		p.write(out);
		out.close();

		ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
		VersionedDataInputStream in = new VersionedDataInputStream(bi, cps);
		Pointcut newP = Pointcut.read(in, null);

		assertEquals("write/read", p, newP);
	}

}
