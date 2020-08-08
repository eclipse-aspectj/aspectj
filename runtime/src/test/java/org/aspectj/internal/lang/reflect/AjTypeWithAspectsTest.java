/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Adrian Colyer       initial implementation 
 * ******************************************************************/
package org.aspectj.internal.lang.reflect;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import junit.framework.TestCase;

import org.aspectj.internal.lang.annotation.ajcDeclareEoW;
import org.aspectj.internal.lang.annotation.ajcPrivileged;
import org.aspectj.lang.annotation.AdviceName;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareError;
import org.aspectj.lang.annotation.DeclareWarning;
import org.aspectj.lang.reflect.Advice;
import org.aspectj.lang.reflect.AdviceKind;
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.DeclareErrorOrWarning;
import org.aspectj.lang.reflect.NoSuchAdviceException;
import org.aspectj.lang.reflect.NoSuchPointcutException;
import org.aspectj.lang.reflect.PerClause;
import org.aspectj.lang.reflect.PerClauseKind;
import org.aspectj.lang.reflect.Pointcut;
import org.aspectj.lang.reflect.PointcutBasedPerClause;
import org.aspectj.lang.reflect.TypePatternBasedPerClause;

public class AjTypeWithAspectsTest extends TestCase {

	private AjType<SimpleAspect> sa;
	
	protected void setUp() throws Exception {
		super.setUp();
		sa = AjTypeSystem.getAjType(SimpleAspect.class);
	}

	public void testGetPerClause() {
		AjType<PerThisAspect> perThisA = AjTypeSystem.getAjType(PerThisAspect.class);
		AjType<PerTargetAspect> perTargetA = AjTypeSystem.getAjType(PerTargetAspect.class);
		AjType<PerCflowAspect> perCflowA = AjTypeSystem.getAjType(PerCflowAspect.class);
		AjType<PerCflowbelowAspect> perCflowbelowA = AjTypeSystem.getAjType(PerCflowbelowAspect.class);
		AjType<PerTypeWithin> perTypeWithinA = AjTypeSystem.getAjType(PerTypeWithin.class);
		
		PerClause pc = perThisA.getPerClause();
		assertEquals(PerClauseKind.PERTHIS,pc.getKind());
		assertEquals("pc()",((PointcutBasedPerClause)pc).getPointcutExpression().asString());
		assertEquals("perthis(pc())",pc.toString());
		
		pc= perTargetA.getPerClause();
		assertEquals(PerClauseKind.PERTARGET,pc.getKind());
		assertEquals("pc()",((PointcutBasedPerClause)pc).getPointcutExpression().asString());
		assertEquals("pertarget(pc())",pc.toString());

		pc= perCflowA.getPerClause();
		assertEquals(PerClauseKind.PERCFLOW,pc.getKind());
		assertEquals("pc()",((PointcutBasedPerClause)pc).getPointcutExpression().asString());
		assertEquals("percflow(pc())",pc.toString());

		pc= perCflowbelowA.getPerClause();
		assertEquals(PerClauseKind.PERCFLOWBELOW,pc.getKind());
		assertEquals("pc()",((PointcutBasedPerClause)pc).getPointcutExpression().asString());
		assertEquals("percflowbelow(pc())",pc.toString());

		pc= perTypeWithinA.getPerClause();
		assertEquals(PerClauseKind.PERTYPEWITHIN,pc.getKind());
		assertEquals("org.aspectj..*",((TypePatternBasedPerClause)pc).getTypePattern().asString());
		assertEquals("pertypewithin(org.aspectj..*)",pc.toString());

	}
	
	public void testGetDeclaredField() throws Exception{
		Field f = sa.getDeclaredField("s");
		try {
			Field f2 = sa.getDeclaredField("ajc$xyz$s");
			fail("Expecting NoSuchFieldException");
		} catch (NoSuchFieldException nsf) {}
	}
	
	public void testGetField() throws Exception {
		Field f = sa.getField("s");
		try {
			Field f2 = sa.getField("ajc$xyz$s");
			fail("Expecting NoSuchFieldException");
		} catch (NoSuchFieldException nsf) {}		
	}
	
	public void testGetDeclaredFields() {
		Field[] fields = sa.getDeclaredFields();
		assertEquals(1,fields.length);
		assertEquals("s",fields[0].getName());
	}

	public void testGetFields() {
		Field[] fields = sa.getFields();
		assertEquals(1,fields.length);
		assertEquals("s",fields[0].getName());
	}
	
	public void testGetDeclaredMethod() throws Exception {
		Method m = sa.getDeclaredMethod("aMethod");
		try {
			Method m2 = sa.getDeclaredMethod("logEntry");
			fail("Expecting NoSuchMethodException");
		} catch(NoSuchMethodException ex) {}
		try {
			Method m3 = sa.getDeclaredMethod("ajc$before$123");
			fail("Expecting NoSuchMethodException");
		} catch(NoSuchMethodException ex) {}
	}
	
	public void testGetMethod() throws Exception {
		Method m = sa.getMethod("aMethod");
		try {
			Method m2 = sa.getMethod("logEntry");
			fail("Expecting NoSuchMethodException");
		} catch(NoSuchMethodException ex) {}
		try {
			Method m3 = sa.getMethod("ajc$before$123");
			fail("Expecting NoSuchMethodException");
		} catch(NoSuchMethodException ex) {}		
	}

	public void testGetDeclaredMethods() {
		Method[] ms = sa.getDeclaredMethods();
		assertEquals(1,ms.length);
		assertEquals("aMethod",ms[0].getName());
	}

	public void testGetMethods() {
		Method[] ms = sa.getMethods();
		assertEquals(10,ms.length);
        //AV was corrupted, cannot rely on ordering
        String match = "";
		for (Method m : ms) {
			match = match + "--" + m.getName();
		}
		assertTrue(match.contains("aMethod"));
	}
	
	public void testGetDeclaredPointcut() throws Exception {
		Pointcut p1 = sa.getDeclaredPointcut("simpleAspectMethodExecution");
		assertEquals("simpleAspectMethodExecution",p1.getName());
		assertEquals("execution(* SimpleAspect.*(..))",p1.getPointcutExpression().asString());
		assertEquals("simpleAspectMethodExecution() : execution(* SimpleAspect.*(..))",p1.toString());
		assertEquals(sa,p1.getDeclaringType());
		assertEquals(0,p1.getParameterTypes().length);
		assertTrue(Modifier.isPublic(p1.getModifiers()));
		Pointcut p2 = sa.getDeclaredPointcut("simpleAspectCall");
		assertEquals("simpleAspectCall",p2.getName());
		assertEquals("call(* SimpleAspect.*(..))",p2.getPointcutExpression().asString());
		assertEquals(sa,p2.getDeclaringType());
		assertEquals(1,p2.getParameterTypes().length);
		assertTrue(Modifier.isPrivate(p2.getModifiers()));
		try {
			Pointcut p3 = sa.getDeclaredPointcut("sausages");
			fail("Expecting NoSuchPointcutExcetpion");
		} catch (NoSuchPointcutException ex) { 
			assertEquals("sausages",ex.getName());
		}		
	}
	
	public void testGetPointcut() throws Exception {
		Pointcut p1 = sa.getPointcut("simpleAspectMethodExecution");
		assertEquals("simpleAspectMethodExecution",p1.getName());
		assertEquals("execution(* SimpleAspect.*(..))",p1.getPointcutExpression().asString());
		assertEquals(sa,p1.getDeclaringType());
		assertEquals(0,p1.getParameterTypes().length);
		assertTrue(Modifier.isPublic(p1.getModifiers()));
		Pointcut p2 = sa.getDeclaredPointcut("simpleAspectCall");
		assertEquals("simpleAspectCall",p2.getName());
		assertEquals("call(* SimpleAspect.*(..))",p2.getPointcutExpression().asString());
		assertEquals(sa,p2.getDeclaringType());
		assertEquals(1,p2.getParameterTypes().length);
		assertTrue(Modifier.isPrivate(p2.getModifiers()));
		try {
			Pointcut p3 = sa.getPointcut("sausages");
			fail("Expecting NoSuchPointcutExcetpion");
		} catch (NoSuchPointcutException ex) { 
			assertEquals("sausages",ex.getName());
		}		
	}
	
	public void testGetDeclaredPointcuts() {
		Pointcut[] pcs = sa.getDeclaredPointcuts();
		assertEquals(2,pcs.length);
        // AV was corrupted, cannot rely on ordering
        String match = "simpleAspectMethodExecution--simpleAspectCall";
		assertTrue(match.contains(pcs[0].getName()));
		assertTrue(match.contains(pcs[1].getName()));
	}
	
	public void testGetPointcuts() {
		Pointcut[] pcs = sa.getPointcuts();
		assertEquals(1,pcs.length);
		assertEquals("simpleAspectMethodExecution",pcs[0].getName());
	}
	
	public void testGetDeclaredAdvice() {
		Advice[] advice = sa.getDeclaredAdvice();
		assertEquals(10,advice.length);
		advice = sa.getDeclaredAdvice(AdviceKind.BEFORE);
		assertEquals(2,advice.length);
		advice = sa.getDeclaredAdvice(AdviceKind.AFTER);
		assertEquals(2,advice.length);
		advice = sa.getDeclaredAdvice(AdviceKind.AFTER_RETURNING);
		assertEquals(2,advice.length);
		advice = sa.getDeclaredAdvice(AdviceKind.AFTER_THROWING);
		assertEquals(2,advice.length);
		advice = sa.getDeclaredAdvice(AdviceKind.AROUND);
		assertEquals(2,advice.length);
		advice = sa.getDeclaredAdvice(AdviceKind.BEFORE,AdviceKind.AFTER);
		assertEquals(4,advice.length);

		advice = sa.getDeclaredAdvice(AdviceKind.BEFORE);
		// AV: corrupted test: cannot rely on ordering since a Set is used behind
        Advice aone, atwo;
        if (advice[0].getName()!=null && advice[0].getName().length()>0) {
            aone = advice[0];
            atwo = advice[1];
        } else {
            aone = advice[1];
            atwo = advice[0];
        }
        assertEquals("execution(* SimpleAspect.*(..))",aone.getPointcutExpression().toString());
        assertEquals("@AdviceName(\"logEntry\") before() : execution(* SimpleAspect.*(..))",aone.toString());
        assertEquals("logEntry",aone.getName());
        assertEquals(AdviceKind.BEFORE,aone.getKind());
        assertEquals("execution(* SimpleAspect.*(..))",atwo.getPointcutExpression().toString());
        assertEquals("",atwo.getName());
        assertEquals("before() : execution(* SimpleAspect.*(..))",atwo.toString());
	}
	
	public void testGetAdvice() {
		Advice[] advice = sa.getDeclaredAdvice();
		assertEquals(10,advice.length);
		advice = sa.getDeclaredAdvice(AdviceKind.BEFORE);
		assertEquals(2,advice.length);
		advice = sa.getDeclaredAdvice(AdviceKind.AFTER);
		assertEquals(2,advice.length);
		advice = sa.getDeclaredAdvice(AdviceKind.AFTER_RETURNING);
		assertEquals(2,advice.length);
		advice = sa.getDeclaredAdvice(AdviceKind.AFTER_THROWING);
		assertEquals(2,advice.length);
		advice = sa.getDeclaredAdvice(AdviceKind.AROUND);
		assertEquals(2,advice.length);
		advice = sa.getDeclaredAdvice(AdviceKind.BEFORE,AdviceKind.AFTER);
		assertEquals(4,advice.length);		
	}
	
	public void testGetNamedAdvice() throws Exception {
		Advice a = sa.getAdvice("logItAll");
		assertEquals("logItAll",a.getName());
		assertEquals(AdviceKind.AROUND,a.getKind());
		a = sa.getAdvice("whatGoesAround");
		assertEquals("whatGoesAround",a.getName());
		assertEquals(AdviceKind.AROUND,a.getKind());
		try {
			a = sa.getAdvice("ajc$after$123");
			fail("Expecting NoSuchAdviceException");
		} catch (NoSuchAdviceException ex) {
			assertEquals("ajc$after$123",ex.getName());
		}
		try {
			a = sa.getAdvice("");
			fail("Expecting IllegalArgumentException");
		} catch (IllegalArgumentException ex) {
			;
		}
	}
	
	public void testGetNamedDeclaredAdvice() throws Exception {
		Advice a = sa.getDeclaredAdvice("logItAll");
		assertEquals("logItAll",a.getName());
		assertEquals(AdviceKind.AROUND,a.getKind());
		a = sa.getDeclaredAdvice("whatGoesAround");
		assertEquals("whatGoesAround",a.getName());
		assertEquals(AdviceKind.AROUND,a.getKind());
		try {
			a = sa.getDeclaredAdvice("ajc$after$123");
			fail("Expecting NoSuchAdviceException");
		} catch (NoSuchAdviceException ex) {
			assertEquals("ajc$after$123",ex.getName());
		}
		try {
			a = sa.getDeclaredAdvice("");
			fail("Expecting IllegalArgumentException");
		} catch (IllegalArgumentException ex) {
			;
		}
	}
	
	public void testIsPrivileged() {
		assertFalse(sa.isPrivileged());
		assertTrue(AjTypeSystem.getAjType(SimplePrivilegedAspect.class).isPrivileged());		
	}
	
	public void testIsAspect() {
		assertTrue(sa.isAspect());
	}
	
	public void testIsMemberAspect() {
		assertFalse(AjTypeSystem.getAjType(SimplePrivilegedAspect.class).isMemberAspect());
		assertTrue(AjTypeSystem.getAjType(SimplePrivilegedAspect.MemberAspect.class).isMemberAspect());

	}
	
	public void testGetDeclareEoWarnings() {
		DeclareErrorOrWarning[] deows = sa.getDeclareErrorOrWarnings();
		assertEquals(4,deows.length);
		boolean foundCodeWarning = false;
		boolean foundCodeError = false;
		boolean foundAnnWarning = false;
		boolean foundAnnError = false;
		for (DeclareErrorOrWarning deow : deows) {
			if (deow.isError()) {
				if (deow.getMessage().equals("dont call this method code")) {
					foundCodeError = true;
					assertEquals("declare error : call(* DontDoIt.*(..)) : \"dont call this method code\"",deow.toString());
				}
				if (deow.getMessage().equals("dont call this method ann")) foundAnnError = true;
				assertEquals("call(* DontDoIt.*(..))",deow.getPointcutExpression().toString());
			} else {
				if (deow.getMessage().equals("dont call this method code")) foundCodeWarning = true;
				if (deow.getMessage().equals("dont call this method ann")) foundAnnWarning = true;
				assertEquals("call(* DontDoIt.*(..))",deow.getPointcutExpression().toString());				
			}
		}
		assertTrue(foundCodeWarning && foundAnnWarning && foundCodeError && foundAnnError);
	}
	
}


@Aspect 
class SimpleAspect {
	
  // regular field
  public String s;
  
  // synthetic field
  public String ajc$xyz$s;
  
  // regular method
  public void aMethod() {}
  
  // advice method, annotation style
  @Before("execution(* SimpleAspect.*(..))")
  public void logEntry() {}
	
  // advice method, code style
  @Before("execution(* SimpleAspect.*(..))")
  public void ajc$before$123() {}

  // advice method, annotation style
  @After("execution(* SimpleAspect.*(..))")
  public void logFinally() {}
	
  // advice method, code style
  @After("execution(* SimpleAspect.*(..))")
  public void ajc$after$123() {}

  // advice method, annotation style
  @AfterReturning("execution(* SimpleAspect.*(..))")
  public void logExit() {}
	
  // advice method, code style
  @AfterReturning("execution(* SimpleAspect.*(..))")
  public void ajc$afterReturning$123() {}

  // advice method, annotation style
  @AfterThrowing("execution(* SimpleAspect.*(..))")
  public void logException() {}
	
  // advice method, code style
  @AfterThrowing("execution(* SimpleAspect.*(..))")
  public void ajc$afterThrowing$123() {}

  // advice method, annotation style
  @Around("execution(* SimpleAspect.*(..))")
  public void logItAll() {}
	
  // advice method, code style
  @Around("execution(* SimpleAspect.*(..))")
  @AdviceName("whatGoesAround")
  public void ajc$around$123() {}

  // pointcut, annotation style
  @org.aspectj.lang.annotation.Pointcut("execution(* SimpleAspect.*(..))")
  public void simpleAspectMethodExecution() {};
  
  // pointcut, code style
  @org.aspectj.lang.annotation.Pointcut("call(* SimpleAspect.*(..))")
  private void ajc$pointcut$$simpleAspectCall$123(SimpleAspect target) {};
  
  // decw, ann style
  @DeclareWarning("call(* DontDoIt.*(..))")
  public static final String dontDoIt = "dont call this method ann";
  
  // decw, code style
  @ajcDeclareEoW(pointcut="call(* DontDoIt.*(..))",message="dont call this method code",isError=false)
  private void ajc$declare_eow$123() {}

  // dec., ann style
  @DeclareError("call(* DontDoIt.*(..))")
  public static final String dontDoItISaid = "dont call this method ann";
  
  // decw, code style
  @ajcDeclareEoW(pointcut="call(* DontDoIt.*(..))",message="dont call this method code",isError=true)
  private void ajc$declare_eow$124() {}
}

@Aspect
@ajcPrivileged
class SimplePrivilegedAspect {
	
	@Aspect
	static class MemberAspect {}
	
}

@Aspect("perthis(pc())")
class PerThisAspect {}

@Aspect("pertarget(pc())")
class PerTargetAspect {}

@Aspect("percflow(pc())")
class PerCflowAspect {}

@Aspect("percflowbelow(pc())")
class PerCflowbelowAspect {}

@Aspect("pertypewithin(org.aspectj..*)") 
class PerTypeWithin {}
