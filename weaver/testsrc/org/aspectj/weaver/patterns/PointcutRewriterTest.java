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

import java.util.Iterator;
import java.util.Set;

import org.aspectj.weaver.Shadow;

import junit.framework.TestCase;

/**
 * @author colyer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PointcutRewriterTest extends TestCase {

	private PointcutRewriter prw;
	
	public void testDistributeNot() {
		Pointcut plain = getPointcut("this(Foo)");
		assertEquals("Unchanged",plain,prw.rewrite(plain));
		Pointcut not = getPointcut("!this(Foo)");
		assertEquals("Unchanged",not,prw.rewrite(not));
		Pointcut notNot = getPointcut("!!this(Foo)");
		assertEquals("this(Foo)",prw.rewrite(notNot).toString());
		Pointcut notNotNOT = getPointcut("!!!this(Foo)");
		assertEquals("!this(Foo)",prw.rewrite(notNotNOT).toString());
		Pointcut and = getPointcut("!(this(Foo) && this(Goo))");
		assertEquals("(!this(Foo) || !this(Goo))",prw.rewrite(and).toString());
		Pointcut or = getPointcut("!(this(Foo) || this(Goo))");
		assertEquals("(!this(Foo) && !this(Goo))",prw.rewrite(or).toString());
		Pointcut nestedNot = getPointcut("!(this(Foo) && !this(Goo))");
		assertEquals("(!this(Foo) || this(Goo))",prw.rewrite(nestedNot).toString());
	}
	
	public void testPullUpDisjunctions() {
		Pointcut aAndb = getPointcut("this(Foo) && this(Goo)");
		assertEquals("Unchanged",aAndb,prw.rewrite(aAndb));
		Pointcut aOrb = getPointcut("this(Foo) || this(Moo)");
		assertEquals("Unchanged",aOrb,prw.rewrite(aOrb));
		Pointcut leftOr = getPointcut("this(Foo) || (this(Goo) && this(Boo))");
		System.out.println(prw.rewrite(leftOr));
		assertEquals("(this(Foo) || (this(Boo) && this(Goo)))",prw.rewrite(leftOr).toString());
		Pointcut rightOr = getPointcut("(this(Goo) && this(Boo)) || this(Foo)");
		assertEquals("(this(Foo) || (this(Boo) && this(Goo)))",prw.rewrite(rightOr).toString());
		Pointcut leftAnd = getPointcut("this(Foo) && (this(Goo) || this(Boo))");
		assertEquals("((this(Boo) && this(Foo)) || (this(Foo) && this(Goo)))",prw.rewrite(leftAnd).toString());
		Pointcut rightAnd = getPointcut("(this(Goo) || this(Boo)) && this(Foo)");
		assertEquals("((this(Boo) && this(Foo)) || (this(Foo) && this(Goo)))",prw.rewrite(rightAnd).toString());
		Pointcut nestedOrs = getPointcut("this(Foo) || this(Goo) || this(Boo)");
		assertEquals("((this(Boo) || this(Foo)) || this(Goo))",prw.rewrite(nestedOrs).toString());
		Pointcut nestedAnds = getPointcut("(this(Foo) && (this(Boo) && (this(Goo) || this(Moo))))");
		// t(F) && (t(B) && (t(G) || t(M)))
		// ==> t(F) && ((t(B) && t(G)) || (t(B) && t(M)))
		// ==> (t(F) && (t(B) && t(G))) || (t(F) && (t(B) && t(M)))
		assertEquals("(((this(Boo) && this(Foo)) && this(Goo)) || ((this(Boo) && this(Foo)) && this(Moo)))",
				prw.rewrite(nestedAnds).toString());
	}
		
	
//	public void testSplitOutWithins() {
//		Pointcut simpleExecution = getPointcut("execution(* *.*(..))");
//		assertEquals("Unchanged",simpleExecution,prw.rewrite(simpleExecution));
//		Pointcut simpleWithinCode = getPointcut("withincode(* *.*(..))");
//		assertEquals("Unchanged",simpleWithinCode,prw.rewrite(simpleWithinCode));
//		Pointcut execution = getPointcut("execution(@Foo Foo (@Goo org.xyz..*).m*(Foo,Boo))");
//		assertEquals("(within((@(Goo) org.xyz..*)) && execution(@(Foo) Foo m*(Foo, Boo)))",
//				 prw.rewrite(execution).toString());
//		Pointcut withincode = getPointcut("withincode(@Foo Foo (@Goo org.xyz..*).m*(Foo,Boo))");
//		assertEquals("(within((@(Goo) org.xyz..*)) && withincode(@(Foo) Foo m*(Foo, Boo)))",
//				 prw.rewrite(withincode).toString());
//		Pointcut notExecution = getPointcut("!execution(Foo BankAccount+.*(..))");
//		assertEquals("(!within(BankAccount+) || !execution(Foo *(..)))",
//				prw.rewrite(notExecution).toString());
//		Pointcut andWithincode = getPointcut("withincode(Foo.new(..)) && this(Foo)");
//		assertEquals("((within(Foo) && withincode(new(..))) && this(Foo))",
//				prw.rewrite(andWithincode).toString());
//		Pointcut orExecution = getPointcut("this(Foo) || execution(Goo Foo.moo(Baa))");
//		assertEquals("((within(Foo) && execution(Goo moo(Baa))) || this(Foo))",
//				prw.rewrite(orExecution).toString());
//	}
	

	public void testRemoveDuplicatesInAnd() {
		Pointcut dupAnd = getPointcut("this(Foo) && this(Foo)");
		assertEquals("this(Foo)",prw.rewrite(dupAnd).toString());
		Pointcut splitdupAnd = getPointcut("(this(Foo) && target(Boo)) && this(Foo)");
		assertEquals("(target(Boo) && this(Foo))",prw.rewrite(splitdupAnd).toString());
	}
	
	public void testNotRemoveNearlyDuplicatesInAnd() {
		Pointcut toAndto = getPointcut("this(Object+) && this(Object)");
		Pointcut rewritten = prw.rewrite(toAndto);
	}
	
	public void testAAndNotAinAnd() {
		Pointcut aAndNota = getPointcut("this(Foo)&& !this(Foo)");
		assertEquals("Matches nothing","",prw.rewrite(aAndNota).toString());
		Pointcut aAndBAndNota = getPointcut("this(Foo) && execution(* *.*(..)) && !this(Foo)");
		assertEquals("Matches nothing","",prw.rewrite(aAndBAndNota).toString());
	}
	
	public void testIfFalseInAnd() {
		Pointcut ifFalse = IfPointcut.makeIfFalsePointcut(Pointcut.CONCRETE);
		Pointcut p = getPointcut("this(A)");
		assertEquals("Matches nothing","",prw.rewrite(new AndPointcut(ifFalse,p)).toString());
	}

	public void testMatchesNothinginAnd() {
		Pointcut nothing = Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
		Pointcut p = getPointcut("this(A)");
		assertEquals("Matches nothing","",prw.rewrite(new AndPointcut(nothing,p)).toString());		
	}
	
	public void testMixedKindsInAnd() {
		Pointcut mixedKinds = getPointcut("call(* *(..)) && execution(* *(..))");
		assertEquals("Matches nothing","",prw.rewrite(mixedKinds).toString());
		Pointcut ok = getPointcut("call(* *(..)) && this(Foo)");
		assertEquals(ok,prw.rewrite(ok));
	}
	
	public void testDetermineKindSetOfAnd() {
		Pointcut oneKind = getPointcut("execution(* foo(..)) && this(Boo)");
		AndPointcut rewritten = (AndPointcut) prw.rewrite(oneKind);
		assertEquals("Only one kind",1,rewritten.couldMatchKinds().size());
		assertTrue("It's Shadow.MethodExecution",rewritten.couldMatchKinds().contains(Shadow.MethodExecution));
	}
	
	public void testKindSetOfExecution() {
		Pointcut p = getPointcut("execution(* foo(..))");
		assertEquals("Only one kind",1,p.couldMatchKinds().size());
		assertTrue("It's Shadow.MethodExecution",p.couldMatchKinds().contains(Shadow.MethodExecution));
		p = getPointcut("execution(new(..))");
		assertEquals("Only one kind",1,p.couldMatchKinds().size());
		assertTrue("It's Shadow.ConstructorExecution",p.couldMatchKinds().contains(Shadow.ConstructorExecution));		
	}
	
	public void testKindSetOfCall() {
		Pointcut p = getPointcut("call(* foo(..))");
		assertEquals("Only one kind",1,p.couldMatchKinds().size());
		assertTrue("It's Shadow.MethodCall",p.couldMatchKinds().contains(Shadow.MethodCall));
		p = getPointcut("call(new(..))");
		assertEquals("Only one kind",1,p.couldMatchKinds().size());
		assertTrue("It's Shadow.ConstructorCall",p.couldMatchKinds().contains(Shadow.ConstructorCall));		
	}
	
	public void testKindSetOfAdviceExecution() {
		Pointcut p = getPointcut("adviceexecution()");
		assertEquals("Only one kind",1,p.couldMatchKinds().size());
		assertTrue("It's Shadow.AdviceExecution",p.couldMatchKinds().contains(Shadow.AdviceExecution));		
	}
	
	public void testKindSetOfGet() {
		Pointcut p = getPointcut("get(* *)");
		assertEquals("Only one kind",1,p.couldMatchKinds().size());
		assertTrue("It's Shadow.FieldGet",p.couldMatchKinds().contains(Shadow.FieldGet));
	}
	
	public void testKindSetOfSet() {
		Pointcut p = getPointcut("set(* *)");
		assertEquals("Only one kind",1,p.couldMatchKinds().size());
		assertTrue("It's Shadow.FieldSet",p.couldMatchKinds().contains(Shadow.FieldSet));						
	}
	
	public void testKindSetOfHandler() {
		Pointcut p = getPointcut("handler(*)");
		assertEquals("Only one kind",1,p.couldMatchKinds().size());
		assertTrue("It's Shadow.ExceptionHandler",p.couldMatchKinds().contains(Shadow.ExceptionHandler));								
	}
	
	public void testKindSetOfInitialization() {
		Pointcut p = getPointcut("initialization(new (..))");
		assertEquals("Only one kind",1,p.couldMatchKinds().size());
		assertTrue("It's Shadow.Initialization",p.couldMatchKinds().contains(Shadow.Initialization));							
	}
	
	public void testKindSetOfPreInitialization() {
		Pointcut p = getPointcut("preinitialization(new (..))");
		assertEquals("Only one kind",1,p.couldMatchKinds().size());
		assertTrue("It's Shadow.PreInitialization",p.couldMatchKinds().contains(Shadow.PreInitialization));									
	}
	
	public void testKindSetOfStaticInitialization() {
		Pointcut p = getPointcut("staticinitialization(*)");
		assertEquals("Only one kind",1,p.couldMatchKinds().size());
		assertTrue("It's Shadow.StaticInitialization",p.couldMatchKinds().contains(Shadow.StaticInitialization));							
	}
	
	public void testKindSetOfThis() {
		Pointcut p = getPointcut("this(Foo)");
		Set matches = p.couldMatchKinds();
		for (Iterator iter = matches.iterator(); iter.hasNext();) {
			Shadow.Kind kind = (Shadow.Kind) iter.next();
			assertFalse("No kinds that don't have a this",kind.neverHasThis());
		}
		for (int i = 0; i < Shadow.SHADOW_KINDS.length; i++) {
			if (!Shadow.SHADOW_KINDS[i].neverHasThis()) {
				assertTrue("All kinds that do have this",matches.contains(Shadow.SHADOW_KINDS[i]));
			}
		}
		// + @
		p = getPointcut("@this(@Foo)");
		matches = p.couldMatchKinds();
		for (Iterator iter = matches.iterator(); iter.hasNext();) {
			Shadow.Kind kind = (Shadow.Kind) iter.next();
			assertFalse("No kinds that don't have a this",kind.neverHasThis());
		}
		for (int i = 0; i < Shadow.SHADOW_KINDS.length; i++) {
			if (!Shadow.SHADOW_KINDS[i].neverHasThis()) {
				assertTrue("All kinds that do have this",matches.contains(Shadow.SHADOW_KINDS[i]));
			}
		}
	}
	
	public void testKindSetOfTarget() {
		Pointcut p = getPointcut("target(Foo)");
		Set matches = p.couldMatchKinds();
		for (Iterator iter = matches.iterator(); iter.hasNext();) {
			Shadow.Kind kind = (Shadow.Kind) iter.next();
			assertFalse("No kinds that don't have a target",kind.neverHasTarget());
		}
		for (int i = 0; i < Shadow.SHADOW_KINDS.length; i++) {
			if (!Shadow.SHADOW_KINDS[i].neverHasTarget()) {
				assertTrue("All kinds that do have target",matches.contains(Shadow.SHADOW_KINDS[i]));
			}
		}
		// + @
		p = getPointcut("@target(@Foo)");
		matches = p.couldMatchKinds();
		for (Iterator iter = matches.iterator(); iter.hasNext();) {
			Shadow.Kind kind = (Shadow.Kind) iter.next();
			assertFalse("No kinds that don't have a target",kind.neverHasTarget());
		}
		for (int i = 0; i < Shadow.SHADOW_KINDS.length; i++) {
			if (!Shadow.SHADOW_KINDS[i].neverHasTarget()) {
				assertTrue("All kinds that do have target",matches.contains(Shadow.SHADOW_KINDS[i]));
			}
		}
	}
	
	public void testKindSetOfArgs() {
		Pointcut p = getPointcut("args(..)");
		assertTrue("All kinds",p.couldMatchKinds().containsAll(Shadow.ALL_SHADOW_KINDS));
		// + @
		p = getPointcut("@args(..)");
		assertTrue("All kinds",p.couldMatchKinds().containsAll(Shadow.ALL_SHADOW_KINDS));
	}
	
	public void testKindSetOfAnnotation() {
		Pointcut p = getPointcut("@annotation(@Foo)");
		assertTrue("All kinds",p.couldMatchKinds().containsAll(Shadow.ALL_SHADOW_KINDS));		
	}
	
	public void testKindSetOfWithin() {
		Pointcut p = getPointcut("within(*)");
		assertTrue("All kinds",p.couldMatchKinds().containsAll(Shadow.ALL_SHADOW_KINDS));
		// + @
		p = getPointcut("@within(@Foo)");
		assertTrue("All kinds",p.couldMatchKinds().containsAll(Shadow.ALL_SHADOW_KINDS));
	}
	
	public void testKindSetOfWithinCode() {
		Pointcut p = getPointcut("withincode(* foo(..))");
		Set matches = p.couldMatchKinds();
		for (Iterator iter = matches.iterator(); iter.hasNext();) {
			Shadow.Kind kind = (Shadow.Kind) iter.next();
			assertFalse("No kinds that are themselves enclosing",
					(kind.isEnclosingKind() && kind != Shadow.ConstructorExecution && kind != Shadow.Initialization));
		}
		for (int i = 0; i < Shadow.SHADOW_KINDS.length; i++) {
			if (!Shadow.SHADOW_KINDS[i].isEnclosingKind()) {
				assertTrue("All kinds that are not enclosing",matches.contains(Shadow.SHADOW_KINDS[i]));
			}
		}	
		assertTrue("Need cons-exe for inlined field inits",matches.contains(Shadow.ConstructorExecution));
		assertTrue("Need init for inlined field inits",matches.contains(Shadow.Initialization));
		// + @
		p = getPointcut("@withincode(@Foo)");
		matches = p.couldMatchKinds();
		for (Iterator iter = matches.iterator(); iter.hasNext();) {
			Shadow.Kind kind = (Shadow.Kind) iter.next();
			assertFalse("No kinds that are themselves enclosing",kind.isEnclosingKind());
		}
		for (int i = 0; i < Shadow.SHADOW_KINDS.length; i++) {
			if (!Shadow.SHADOW_KINDS[i].isEnclosingKind()) {
				assertTrue("All kinds that are not enclosing",matches.contains(Shadow.SHADOW_KINDS[i]));
			}
		}		
	}
	
	public void testKindSetOfIf() {
		Pointcut p = new IfPointcut(null,0);
		assertTrue("All kinds",p.couldMatchKinds().containsAll(Shadow.ALL_SHADOW_KINDS));
		p = IfPointcut.makeIfTruePointcut(Pointcut.CONCRETE);
		assertTrue("All kinds",p.couldMatchKinds().containsAll(Shadow.ALL_SHADOW_KINDS));
		p = IfPointcut.makeIfFalsePointcut(Pointcut.CONCRETE);
		assertTrue("Nothing",p.couldMatchKinds().isEmpty());
	}
	
	public void testKindSetOfCflow() {
		Pointcut p = getPointcut("cflow(this(Foo))");
		assertTrue("All kinds",p.couldMatchKinds().containsAll(Shadow.ALL_SHADOW_KINDS));
		// [below]
		p = getPointcut("cflowbelow(this(Foo))");
		assertTrue("All kinds",p.couldMatchKinds().containsAll(Shadow.ALL_SHADOW_KINDS));
	}
	
	public void testKindSetInNegation() {
		Pointcut p = getPointcut("!execution(new(..))");
		assertTrue("All kinds",p.couldMatchKinds().containsAll(Shadow.ALL_SHADOW_KINDS));		
	}
	
	public void testKindSetOfOr() {
		Pointcut p = getPointcut("execution(new(..)) || get(* *)");
		Set matches = p.couldMatchKinds();
		assertEquals("2 kinds",2,matches.size());
		assertTrue("ConstructorExecution",matches.contains(Shadow.ConstructorExecution));
		assertTrue("FieldGet",matches.contains(Shadow.FieldGet));
	}
	
	public void testOrderingInAnd() {
		Pointcut bigLongPC = getPointcut("cflow(this(Foo)) && @args(@X) && args(X) && @this(@Foo) && @target(@Boo) && this(Moo) && target(Boo) && @annotation(@Moo) && @withincode(@Boo) && withincode(new(..)) && set(* *)&& @within(@Foo) && within(Foo)");
		Pointcut rewritten = prw.rewrite(bigLongPC);
		assertEquals("((((((((((((within(Foo) && @within(@Foo)) && set(* *)) && withincode(new(..))) && @withincode(@Boo)) && @annotation(@Moo)) && target(Boo)) && this(Moo)) && @target(@Boo)) && @this(@Foo)) && args(X)) && @args(@X)) && cflow(this(Foo)))",rewritten.toString());
	}
	
	public void testOrderingInSimpleOr() {
		OrPointcut opc = (OrPointcut) getPointcut("execution(new(..)) || get(* *)");
		assertEquals("reordered","(get(* *) || execution(new(..)))",prw.rewrite(opc).toString());
	}
	
	public void testOrderingInNestedOrs() {
		OrPointcut opc = (OrPointcut) getPointcut("(execution(new(..)) || get(* *)) || within(abc)");
		assertEquals("reordered","((within(abc) || get(* *)) || execution(new(..)))",
				prw.rewrite(opc).toString());
	}
	
	public void testOrderingInOrsWithNestedAnds() {
		OrPointcut opc = (OrPointcut) getPointcut("get(* *) || (execution(new(..)) && within(abc))");
		assertEquals("reordered","((within(abc) && execution(new(..))) || get(* *))",
				prw.rewrite(opc).toString());
	}
		
	private Pointcut getPointcut(String s) {
		return new PatternParser(s).parsePointcut();
	}
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		prw = new PointcutRewriter();
	}

}
