/* *******************************************************************
 * Copyright (c) 2003-2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg     initial implementation 
 * ******************************************************************/

// START-SAMPLE library-pointcutIdioms Standard pointcut idioms 
package org.aspectj.lib.pointcuts;

import java.util.Collection;
import java.io.PrintStream;

/**
 * Library of pointcut idioms to use in combination with
 * other pointcuts.
 * 
 * @author Wes Isberg
 */
public final class Pointcuts {

    // ------- not staticly-determinable
    public pointcut adviceCflow() : cflow(adviceexecution());

    public pointcut notInAdviceCflow() : !adviceCflow();

    public pointcut cflowMainExecution() :
        cflow(mainExecution());

    // ------- staticly-determinable

    public pointcut mainExecution() :
        execution(public static void main(String[]));
        
    /** staticly-determinable to never match any join point */
    public pointcut never();
    
    public pointcut afterAdviceSupported() : !handler(*);

    public pointcut aroundAdviceSupported() : !handler(*)
        && !initialization(new(..)) && !preinitialization(new(..));

    public pointcut anyMethodExecution() : 
        execution(* *(..));

    public pointcut anyPublicMethodExecution() : 
        execution(public * *(..));

    public pointcut anyPackageProtectedMethodExecution() : 
        execution(!private !public !protected * *(..));

    public pointcut anyNonPrivateMethodExecution() : 
        execution(!private * *(..));

    public pointcut anyConstructorExecution() : 
        execution(new(..));

    public pointcut anyPublicConstructorExecution() : 
        execution(public new(..));

    public pointcut anyNonPrivateConstructorExecution() :
        execution(!private new(..));

    public pointcut anyPublicFieldGet() : 
        get(public * *);

    public pointcut anyNonPrivateFieldGet() : 
        get(!private * *);

    public pointcut anyPublicFieldSet() : 
        set(public * *);

    public pointcut anyNonPrivateFieldSet() : 
        set(!private * *); // also !transient?

    public pointcut withinSetter() :  // require !static?
        withincode(void set*(*)); // use any return type? multiple parms?

    public pointcut withinGetter() : 
        withincode(!void get*()); // permit parms? require !static?
    
    public pointcut anyNonPublicFieldSetOutsideConstructorOrSetter() : 
        set(!public * *) && !withincode(new(..)) 
        && !withinSetter();

    public pointcut anyRunnableImplementation() :
        staticinitialization(Runnable+);

    public pointcut anyGetSystemErrOut() :
        get(PrintStream System.err) || get(PrintStream System.out);

    public pointcut anySetSystemErrOut() :
        call(void System.setOut(..)) || call(void System.setErr(..));
    
    public pointcut withinAnyJavaCode() :
        within(java..*) || within(javax..*);

    public pointcut notWithinJavaCode() :
        !withinAnyJavaCode();

    public pointcut toStringExecution() :
        execution(String toString()) && !within(String);

    /** call or execution of any Thread constructor, including subclasses */
    public pointcut anyThreadConstruction() :
        call(Thread+.new(..)) || execution(Thread+.new(..));

    /** 
     * Any calls to java.io classes 
     * (but not methods declared only on their subclasses).
     */
    public pointcut anyJavaIOCalls() :
        call(* java.io..*.*(..)) || call(java.io..*.new(..));

    /** 
     * Any calls to java.awt or javax.swing methods or constructors 
     * (but not methods declared only on their subclasses).
     */
    public pointcut anyJavaAWTOrSwingCalls() :
        call(* java.awt..*.*(..)) || call(java.awt..*.new(..))
        || call(* javax.swing..*.*(..)) || call(javax.swing..*.new(..));

    public pointcut cloneImplementationsInNonCloneable() :
        execution(Object !Cloneable+.clone());
        
    public pointcut runImplementationsInNonRunnable() :
        execution(void !Runnable+.run());
        
    /** any calls to java.lang.reflect or Class.get* (except getName()) */
    public pointcut anySystemReflectiveCalls() :
        call(* java.lang.reflect..*.*(..))
        || (!call(* Class.getName())
            && call(* Class.get*(..)));

    /** standard class-loading calls by Class and ClassLoader
     * Note that `Foo.class` in bytecode is `Class.forName("Foo")`,
     * so 'Foo.class' will also be picked out by this pointcut.
     */
    public pointcut anySystemClassLoadingCalls() :
        call(Class Class.forName(..))
        || call(Class ClassLoader.loadClass(..));

    public pointcut anySystemProcessSpawningCalls() :
        call(Process Runtime.exec(..))
        || call(Class ClassLoader.loadClass(..));

    /** Write methods on Collection
     * Warning: Does not pick out <code>iterator()</code>, even though
     * an Iterator can remove elements.
     */
    public pointcut anyCollectionWriteCalls() :
        call(boolean Collection+.add(Object)) 
        || call(boolean Collection+.addAll(Collection)) 
        || call(void Collection+.clear())
        || call(boolean Collection+.remove(Object))
        || call(boolean Collection+.removeAll(Collection))
        || call(boolean Collection+.retainAll(Collection));
        
    public pointcut mostThrowableReadCalls() :
        call(* Throwable+.get*(..))
        || call(* Throwable+.print*(..))
        || call(String Throwable+.toString(..));

    public pointcut exceptionWrappingCalls() :
        (args(Throwable+,..) || args(.., Throwable+))
        && (set(Throwable+ Throwable+.*)
            || (call(* Throwable+.*(..)) 
                || call(Throwable+.new(..))));

    public pointcut anyCodeThrowingException() :
        execution(* *(..) throws Exception+)
            || execution(new(..) throws Exception+);
            
    private Pointcuts() {} 
}
//END-SAMPLE library-pointcutIdioms


