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


package org.aspectj.weaver.bcel;

import java.util.*;
import java.io.*;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.*;
import org.aspectj.weaver.bcel.*;
import org.aspectj.weaver.*;

public class TjpWeaveTestCase extends WeaveTestCase {
	{
		regenerate = false;
	}

	public TjpWeaveTestCase(String name) {
		super(name);
	}
    

    public void testStaticTjp() throws IOException {
    	BcelAdvice munger = new BcelAdvice(
    		AdviceKind.stringToKind("before"),
    		makePointcutAll(), 
      		Member.methodFromString("static void Aspect.ajc_before(org.aspectj.lang.JoinPoint$StaticPart)"),
    		Advice.ThisJoinPointStaticPart, -1, -1, null, null);
    	
        weaveTest("HelloWorld", "StaticTjpBeforeHelloWorld", munger);
    } 
    
    
    public void testEnclosingStaticTjp() throws IOException {
    	BcelAdvice munger = new BcelAdvice(
    		AdviceKind.stringToKind("before"),
    		makePointcutAll(), 
      		Member.methodFromString("static void Aspect.ajc_before(org.aspectj.lang.JoinPoint$StaticPart)"),
    		Advice.ThisEnclosingJoinPointStaticPart, -1, -1, null, null);
    	
        weaveTest("HelloWorld", "StaticEnclosingTjpBeforeHelloWorld", munger);
    } 


    public void testTjp() throws IOException {
    	BcelAdvice munger = new BcelAdvice(
    		AdviceKind.stringToKind("before"),
    		makePointcutAll(), 
      		Member.methodFromString("static void Aspect.ajc_before(org.aspectj.lang.JoinPoint)"),
    		Advice.ThisJoinPoint, -1, -1, null, null);
    	
        weaveTest("HelloWorld", "TjpBeforeHelloWorld", munger);
    } 

    public void testAroundTjp() throws IOException {
    	BcelAdvice munger = new BcelAdvice(
    		AdviceKind.stringToKind("around"),
    		makePointcutAll(), 
      		Member.methodFromString("static java.lang.Object Aspect.ajc_around(org.aspectj.runtime.internal.AroundClosure, org.aspectj.lang.JoinPoint)"),
    		Advice.ThisJoinPoint | Advice.ExtraArgument, -1, -1, null, null);
    	
        weaveTest("HelloWorld", "TjpAroundHelloWorld", munger);
    } 

    public void testAround2Tjp() throws IOException {
    	BcelAdvice munger1 = new BcelAdvice(
    		AdviceKind.stringToKind("around"),
    		makePointcutAll(), 
      		Member.methodFromString("static java.lang.Object Aspect.ajc_around(org.aspectj.runtime.internal.AroundClosure, org.aspectj.lang.JoinPoint)"),
    		Advice.ThisJoinPoint | Advice.ExtraArgument, -1, -1, null, 
    		world.resolve(TypeX.forName("Aspect")));
    	
    	BcelAdvice munger2 = new BcelAdvice(
    		AdviceKind.stringToKind("around"),
    		makePointcutAll(), 
      		Member.methodFromString("static java.lang.Object Aspect.ajc_around(org.aspectj.runtime.internal.AroundClosure, org.aspectj.lang.JoinPoint)"),
    		Advice.ThisJoinPoint | Advice.ExtraArgument, -1, -1, null, 
    		world.resolve(TypeX.forName("Aspect")));
    	
        weaveTest("HelloWorld", "TjpAround2HelloWorld", Arrays.asList(new BcelAdvice[] {munger1, munger2}));
    } 




}
