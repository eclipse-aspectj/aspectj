/* This file is part of the compiler and core tools for the AspectJ(tm)
 * programming language; see http://aspectj.org
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * either http://www.mozilla.org/MPL/ or http://aspectj.org/MPL/.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is AspectJ.
 *
 * The Initial Developer of the Original Code is Palo Alto Research Center,
 * Incorporated (PARC). Portions created by PARC are are
 * Copyright (C) 2002 Palo Alto Research Center, Incorporated.
 * All Rights Reserved.
 *
 * Contributor(s):
 */
package org.aspectj.weaver.test;
import java.util.*;

import org.aspectj.runtime.internal.*;
import org.aspectj.runtime.internal.AroundClosure;
import org.aspectj.lang.JoinPoint;

public class Aspect {

	public static void ajc_before_0() {
		System.out.println("before_0");		
	}
 	public static void ajc_before_0(String s) {
		System.out.println("before_0: " + s);		
	}
    public static boolean ajc_around_0(ArrayList s, AroundClosure c) throws Throwable {
        System.out.println("doing around, got " + s);
        Object ret = c.run(new Object[] {s}); // proceed(s)
        return ((Boolean) ret).booleanValue();   
    }

	public static void ajc_before_0(java.util.ArrayList list) {
		System.out.println("before_0: " + list);		
	}

	public static void ajc_before_method_execution() {

	}	
	public static void ajc_before_method_execution(Object o) {
		System.out.println("before_method_execution: " + o);
	}

    public static void ajc_after_method_execution() {
        System.out.println("after_method_execution");
    }    
    public static void ajc_after_method_execution(Object o) {
        System.out.println("after_method_execution: " + o);
    }
    
    public static void ajc_afterReturning_method_execution() {
        System.out.println("ajc_afterReturning_method_execution");
    }           	
	public static void ajc_afterReturning_method_execution(Object o) {
		System.out.println("afterReturning_method_execution: " + o);
	}

    public static void ajc_afterThrowing_method_execution() {
        System.out.println("ajc_afterThrowing_method_execution");
    }               
    public static void ajc_afterThrowing_method_execution(Object o) {
        System.out.println("afterThrowing_method_execution: " + o);
    }    




        
    public static Object ajc_around(AroundClosure closure) throws Throwable {
        Object ret = closure.run(new Object[] {});
        return ret;
    }   
    
    public static Object ajc_around(AroundClosure closure, JoinPoint tjp) throws Throwable {
        System.out.println("thisJoinPoint: " + tjp);
        Object ret = closure.run(new Object[] {});
        return ret;
    }   
    
    // ---    
    
    
    public static void ajc_before_method_call() {
        System.out.println("before_method_call");
    }    
    public static void ajc_before_method_call(Object o) {
        System.out.println("before_method_call: " + o);
    }
        
    public static void ajc_after_method_call() {
        System.out.println("after_method_call");
    }    
    public static void ajc_after_method_call(Object o) {
        System.out.println("after_method_call: " + o);
    }       
	
    public static void ajc_afterReturning_method_call() {
        System.out.println("ajc_afterReturning_method_call");
    }
	public static void ajc_afterReturning_method_call(Object o) {
		System.out.println("afterReturning_method_call: " + o);
	}
	
    public static void ajc_afterThrowing_method_call() {
        System.out.println("ajc_afterThrowing_method_call");
    }
    public static void ajc_afterThrowing_method_call(Object o) {
        System.out.println("afterThrowing_method_call: " + o);
    }    
    
    public static Object ajc_around_method_call(AroundClosure closure) throws Throwable {
        Object ret = null;
        for (int i=0; i<3; i++) {
            System.out.println("enter: " + i);
            ret = closure.run(new Object[] {});
        }
        return ret;
    }
    
    // ----

    public static void ajc_before_constructor_call() {
        System.out.println("before_constructor_call");
    }    
    public static void ajc_before_constructor_call(Object o) {
        System.out.println("before_constructor_call: " + o);
    }
        
    public static void ajc_after_constructor_call() {
        System.out.println("after_constructor_call");
    }    
    public static void ajc_after_constructor_call(Object o) {
        System.out.println("after_constructor_call: " + o);
    }       
	
    public static void ajc_afterReturning_constructor_call() {
        System.out.println("ajc_afterReturning_constructor_call");
    }
	public static void ajc_afterReturning_constructor_call(Object o) {
		System.out.println("afterReturning_constructor_call: " + o);
	}
	
    public static void ajc_afterThrowing_constructor_call() {
        System.out.println("ajc_afterThrowing_constructor_call");
    }
    public static void ajc_afterThrowing_constructor_call(Object o) {
        System.out.println("afterThrowing_constructor_call: " + o);
    }    
    
    public static Object ajc_around_constructor_call(AroundClosure closure) throws Throwable {
        Object ret = null;
        for (int i=0; i<3; i++) {
            System.out.println("enter: " + i);
            ret = closure.run(new Object[] {});
        }
        return ret;
    }    
    // ----

    public static void ajc_before_constructor_execution() {
        System.out.println("before_constructor_execution");
    }    
    public static void ajc_before_constructor_execution(Object o) {
        System.out.println("before_constructor_execution: " + o);
    }
        
    public static void ajc_after_constructor_execution() {
        System.out.println("after_constructor_execution");
    }    
    public static void ajc_after_constructor_execution(Object o) {
        System.out.println("after_constructor_execution: " + o);
    }       
	
    public static void ajc_afterReturning_constructor_execution() {
        System.out.println("ajc_afterReturning_constructor_execution");
    }
	public static void ajc_afterReturning_constructor_execution(Object o) {
		System.out.println("afterReturning_constructor_execution: " + o);
	}
	
    public static void ajc_afterThrowing_constructor_execution() {
        System.out.println("ajc_afterThrowing_constructor_execution");
    }
    public static void ajc_afterThrowing_constructor_execution(Object o) {
        System.out.println("afterThrowing_constructor_execution: " + o);
    }    
    
    public static Object ajc_around_constructor_execution(AroundClosure closure) throws Throwable {
        Object ret = null;
        for (int i=0; i<3; i++) {
            System.out.println("enter: " + i);
            ret = closure.run(new Object[] {});
        }
        return ret;
    }    
    
    
    // ---

	
	public static void ajc_before_field_get() {
		System.out.println("before_field_get");
	}   
    public static void ajc_before_field_get(Object o) {
        System.out.println("before_field_get: " + o);
    }
    
    public static void ajc_after_field_get() {
        System.out.println("after_field_get");
    }    
    public static void ajc_after_field_get(Object o) {
        System.out.println("after_field_get: " + o);
    }
    
    public static void ajc_afterReturning_field_get() {
        System.out.println("afterReturning_field_get");
    }    
    public static void ajc_afterReturning_field_get(Object o) {
        System.out.println("afterReturning_field_get: " + o);
    }

    public static void ajc_afterThrowing_field_get() {
        System.out.println("afterThrowing_field_get");
    }    
    public static void ajc_afterThrowing_field_get(Object o) {
        System.out.println("afterThrowing_field_get: " + o);
    }
    public static void ajc_afterThrowing_field_get(Throwable t) {
        System.out.println("afterThrowing_field_get: " + t);
    }

	public static Object ajc_around_field_get(AroundClosure closure) throws Throwable {
		Object ret = closure.run(new Object[] {});
		return ret;
	}
	
   
    // ---

	
	public static void ajc_before_field_set() {
		System.out.println("before_field_set");
	}   
    public static void ajc_before_field_set(Object o) {
        System.out.println("before_field_set: " + o);
    }
    
    public static void ajc_after_field_set() {
        System.out.println("after_field_set");
    }    
    public static void ajc_after_field_set(Object o) {
        System.out.println("after_field_set: " + o);
    }
    
    public static void ajc_afterReturning_field_set() {
        System.out.println("afterReturning_field_set");
    }    
    public static void ajc_afterReturning_field_set(Object o) {
        System.out.println("afterReturning_field_set: " + o);
    }

    public static void ajc_afterThrowing_field_set() {
        System.out.println("afterThrowing_field_set");
    }    
    public static void ajc_afterThrowing_field_set(Object o) {
        System.out.println("afterThrowing_field_set: " + o);
    }
    public static void ajc_afterThrowing_field_set(Throwable t) {
        System.out.println("afterThrowing_field_set: " + t);
    }

	public static Object ajc_around_field_set(AroundClosure closure) throws Throwable {
		Object ret = closure.run(new Object[] {});
		return ret;
	}	
	
	// don't call this method for callee-side call join points
	public static void ajc_before(JoinPoint.StaticPart tjp) {
		System.out.println("before: " + tjp);
		if (tjp.getSourceLocation() == null) {
			throw new RuntimeException("didn't want null");
		}
		System.out.println("   loc: " + tjp.getSourceLocation());
	}
	
	public static void ajc_before(JoinPoint tjp) {
		System.out.println("before: " + tjp + " this = " + tjp.getThis() + 
				" target = " + tjp.getTarget() +
				" args = " + Arrays.asList(tjp.getArgs()));
	}
	
	// per object stuff
	
	private static Map objects = new HashMap();
	
	public static void ajc$perObjectBind(Object o) {
		if (objects.containsKey(o)) return;
		objects.put(o, new Aspect());
	}
	
	public static boolean hasAspect(Object o) {
		return objects.containsKey(o);
	}
	
	public static Aspect aspectOf(Object o) {
		return (Aspect) objects.get(o);
	}	
	
	
	// per cflow stuff
	
	public static void ajc$perCflowPush() {
		ajc$perCflowStack.pushInstance(new Aspect());
	}
	
	public static boolean hasAspect() {
		return ajc$perCflowStack.isValid();
	}
	
	public static Aspect aspectOf() {
		if (ajc$perSingletonInstance != null) return ajc$perSingletonInstance;
		
		return (Aspect) ajc$perCflowStack.peekInstance();
	}
	
	public static CFlowStack ajc$perCflowStack = new CFlowStack();
	
	// non-static methods
	
	public static Aspect ajc$perSingletonInstance = new Aspect();
	public void ajc_before() {
		System.out.println("before in: " + this);
	}
	
    public static CFlowStack ajc$cflowStack$0 = new CFlowStack();
	
	
}
