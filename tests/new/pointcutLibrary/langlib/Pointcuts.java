/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg     initial implementation 
 * ******************************************************************/

// START-SAMPLE library-pointcutIdioms Standard pointcut idioms 
package langlib;

import java.io.*;

/**
 * Library of pointcut idioms to use in combination with
 * other pointcuts.
 * 
 * @author Wes Isberg
 */
public class Pointcuts {

    // ------- not staticly-determinable
    public pointcut adviceCflow() : cflow(adviceexecution());

    public pointcut notInAdviceCflow() : !adviceCflow();

    public pointcut cflowMainExecution() :
        cflow(mainExecution());

    // ------- staticly-determinable

    public pointcut mainExecution() :
        execution(public static void main(String[]));
        
    /** staticly-determinable to never match any join point */
    public pointcut never() : if(false) 
        && execution(ThreadDeath *(ThreadDeath, ThreadDeath));
        
    public pointcut anyMethodExecution() : 
        execution(* *(..));

    public pointcut anyPublicMethodExecution() : 
        execution(public * *(..));

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

    public pointcut withinSetter() : 
        withincode(* set*(..));

    public pointcut withinGetter() : 
        withincode(Object+ get*(..));
    
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
     * Any calls to java.awt or javax.swing classes 
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

    /** standard class-loading calls by Class and ClassLoader */
    public pointcut anySystemClassLoadingCalls() :
        call(Class Class.forName(..))
        || call(Class ClassLoader.loadClass(..));

    public pointcut anySystemProcessSpawningCalls() :
        call(Process Runtime.exec(..))
        || call(Class ClassLoader.loadClass(..));

    public pointcut mostThrowableReadCalls() :
        call(* Throwable+.get*(..))
        || call(* Throwable+.print*(..))
        || call(String Throwable+.toString(..));

    public pointcut exceptionWrappingCalls() :
        (args(Throwable+,..) || args(.., Throwable+))
        && (set(Throwable+ Throwable+.*)
            || (call(* Throwable+.*(..)) 
                || call(Throwable+.new(..))));

    private Pointcuts() {}
    
}
aspect A {
    private static aspect PointcutsOnly {
        /** require this library to only contain pointcuts */
        declare error : within(Pointcuts) &&
            (Pointcuts.anyMethodExecution() 
                || Pointcuts.anyNonPrivateConstructorExecution()
                || set(* *)) : "only pointcuts permitted in Pointcuts";
             // does not pick out field definitions -- too costly
             // set(* Pointcuts.*) || get(* Pointcuts.*)
    }
}
// END-SAMPLE library-pointcutIdioms

class PointcutQuestions {
    public pointcut anyCodeThrowingException() : // XXX broken?
        execution(* *(..) throws Exception+)
        || execution(new(..) throws Exception+);

}
