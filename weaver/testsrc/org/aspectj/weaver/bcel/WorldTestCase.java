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

import java.lang.reflect.Modifier;

import org.aspectj.weaver.*;

/**
 * This is a test case for the nameType parts of worlds.
 */
public class WorldTestCase extends AbstractWorldTestCase {

    public WorldTestCase(String name) {
        super(name);
    }
        
    private final BcelWorld world 
        = new BcelWorld(BcweaverTests.TESTDATA_PATH + "/tracing.jar");

	protected World getWorld() {
		return world;
	}

	// XXX fix the various XXXs before expecting this test to work
    public void xtestTraceJar() {
        ResolvedTypeX trace = world.resolve(TypeX.forName("Trace"));

        fieldsTest(trace, Member.NONE);
        Member constr = Member.methodFromString("void Trace.<init>()"); 
        //XXX need attribute fix - 
        //methodsTest(trace, new Member[] { constr });

        interfacesTest(trace, ResolvedTypeX.NONE);
        superclassTest(trace, TypeX.OBJECT);
        isInterfaceTest(trace, false);
        isClassTest(trace, false);
        isAspectTest(trace, true);

        pointcutsTest(trace, 
            new Member[] {
                Member.pointcut(trace, "traced", "(Ljava/lang/Object;)V"),
            });

        modifiersTest(trace.findPointcut("traced"), 
            Modifier.PUBLIC | Modifier.ABSTRACT);
        
        mungersTest(trace, 
            new ShadowMunger[] {
				world.shadowMunger("before(foo): traced(foo) -> void Trace.ajc_before_4(java.lang.Object))",
            					0),
				world.shadowMunger("afterReturning(foo): traced(foo) -> void Trace.ajc_afterreturning_3(java.lang.Object, java.lang.Object))",
            					Advice.ExtraArgument),
				world.shadowMunger("around(): execution(* doit(..)) -> java.lang.Object Trace.ajc_around_2(org.aspectj.runtime.internal.AroundClosure))",
            					Advice.ExtraArgument),
				world.shadowMunger("around(foo): traced(foo) -> java.lang.Object Trace.ajc_around_1(java.lang.Object, org.aspectj.runtime.internal.AroundClosure))",
            					Advice.ExtraArgument),
            });
        
        ResolvedTypeX myTrace = world.resolve(TypeX.forName("MyTrace"));

        interfacesTest(myTrace, ResolvedTypeX.NONE);
        superclassTest(myTrace, trace);
        isInterfaceTest(myTrace, false);
        isClassTest(myTrace, false);
        isAspectTest(myTrace, true);

        //XXX need attribute fix - 
        //fieldsTest(myTrace, Member.NONE);


        pointcutsTest(trace, 
            new Member[] {
                Member.pointcut(trace, "traced", "(Ljava/lang/Object;)V"),
            });

        modifiersTest(myTrace.findPointcut("traced"), 
            Modifier.PUBLIC);
        
        // this tests for declared mungers
        mungersTest(myTrace, ShadowMunger.NONE);        
        
    }

    public void testIterator() {
        int abstractPublic = Modifier.ABSTRACT | Modifier.PUBLIC;
        ResolvedTypeX iter = world.resolve(TypeX.forName("java.util.Iterator"));
      
        modifiersTest(iter, abstractPublic | Modifier.INTERFACE);
        fieldsTest(iter, ResolvedMember.NONE);
        methodsTest(iter, 
            new Member[] {
                Member.method(iter, 0, "hasNext", "()Z"),
                Member.method(iter, 0, "remove", "()V"),
                Member.method(iter, 0, "next", "()Ljava/lang/Object;"),
                });
        ResolvedMember remove = iter.lookupMethod(Member.method(iter, 0, "remove", "()V"));
        assertNotNull("iterator doesn't have remove" , remove);
        modifiersTest(remove, abstractPublic | Modifier.INTERFACE);
        exceptionsTest(remove, TypeX.NONE);

        ResolvedMember clone = iter.lookupMethod(Member.method(TypeX.OBJECT, 0, "clone", "()Ljava/lang/Object;"));
        assertNotNull("iterator doesn't have clone" , clone);
        modifiersTest(clone, Modifier.PROTECTED | Modifier.NATIVE);
        exceptionsTest(clone, TypeX.forNames(new String[] {"java.lang.CloneNotSupportedException"}));

        interfacesTest(iter, ResolvedTypeX.NONE);
        superclassTest(iter, TypeX.OBJECT);
        pointcutsTest(iter, ResolvedMember.NONE);
        mungersTest(iter, ShadowMunger.NONE);
        isInterfaceTest(iter, true);
        isClassTest(iter, false);
        isAspectTest(iter, false);
    }

	public void testObjectCoersion() {
		assertCouldBeCoercibleFrom("java.lang.Object", "java.lang.String");
		assertCouldBeCoercibleFrom("java.lang.Integer", "java.lang.Object");
		assertCouldBeCoercibleFrom("java.io.Serializable", "java.lang.Runnable");		
		assertCouldBeCoercibleFrom("java.util.Stack", "java.lang.Runnable");		
		assertCouldNotBeCoercibleFrom("java.lang.Runnable", "java.lang.Integer");				
		assertCouldNotBeCoercibleFrom("java.lang.Integer", "java.lang.String");			
		assertCouldNotBeCoercibleFrom("java.lang.Integer", "java.lang.Runnable");							
	}

	// ----

	private void assertCouldBeCoercibleFrom(String a, String b) {
		isCoerceableFromTest(world.resolve(a), world.resolve(b), true);
	}

	private void assertCouldNotBeCoercibleFrom(String a, String b) {
		isCoerceableFromTest(world.resolve(a), world.resolve(b), false);
	}	    


}
