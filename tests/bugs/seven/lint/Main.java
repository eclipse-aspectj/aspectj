/*
 * Copyright (c) 2005 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *
 * Created on 25.03.2005
 * 
 * Contributors
 *  Oliver Boehm               initial implementation
 */

import java.io.Serializable;
import org.aspectj.lang.annotation.SuppressAjWarnings;

/**
 * Test Aspect to check the different Xlint warnings
 */
aspect XlintTest {
    
    /*
     * examples for "invalidAbsoluteTypeName"
     */
 
    pointcut correctName() :
        call(String java.lang.Object.toString());
    
    pointcut wrongPackageName() :
        call(String java.xxx.Object.toString());
    
    pointcut wrongTypeName() :
        call(String java.lang.Xxx.toString());
    
    /** no warning!!! */
    pointcut wrongMethodName() :
        call(String java.lang.Object.xxx());
    
    @SuppressAjWarnings
    after() : call(String java.lang.Xxx.toString()) {
        System.out.println(thisJoinPoint);
    }
    
    
    
    /*
     * no example for "invalidWildcardTypeName"
     * 
     * Never signalled anywhere in the codebase
     * @see http://dev.eclipse.org/mhonarc/lists/aspectj-dev/msg01404.html
     */

    
    
    /*
     * example for "unresolvableMember"
     * 
     * hard to reproduce - I tried different things but at last I give up
     * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=59596#c2
     */
    
    
    
    /*
     * example for "typeNotExposedToWeaver"
     */
    
    public int Object.dummy = 0;
    
    
    
    /*
     * no example for "shadowNotInStructure"
     * 
     * Signalled if the structure model is broken, probably can't happen
     * @see http://dev.eclipse.org/mhonarc/lists/aspectj-dev/msg01404.html
     */
    


    /*
     * example for "unmatchedSuperTypeInCall"
     */

    pointcut unmatchedToStringCall() :
        call(String Car.toString());

    pointcut matchedToStringCall() :
        call(String Object.toString()) && target(Car);
    
    before() : unmatchedToStringCall() && !within(XlintTest) {
        System.out.println(thisJoinPoint);
    }
        
    @SuppressAjWarnings
    before() : call(String Car.toString()) {
        System.out.println(thisJoinPoint);
    }
    
    @SuppressAjWarnings({"adviceDidNotMatch"})
    before() : call(* java.lang.String.helloWorld()) {
        System.out.println(thisJoinPoint);
    }
    
        
    /*
     * example for "canNotImplementLazyTjp"
     * 
     * This example is from the README-12.html. To get the warning you must
     * compile it with "-XlazyTjp"
     * 
     * NOTE: The expected warnung does not appear. I don't know why.
     *       Here is the commandline:
     *       ajc -XlazyTjp -Xlint:warning -inpath src -d classes src
     */
    
    public static boolean enabled = false;
    
    pointcut toBeTraced() : execution(* *(..)) && !within(XlintTest);

    Object around() : toBeTraced() && if(enabled) {
        Object[] args = thisJoinPoint.getArgs();
        System.out.println(thisJoinPoint + ", arg's: " + args.length);
        return proceed();
    }

    
    
    /*
     * example for "needsSerialVersionUIDField"
     */
    
    declare parents : Main implements java.io.Serializable;
    
    
    
    /*
     * example for "brokeSerialVersionCompatibility"
     * 
     * NOTE: I don't see this warning inside Eclipse with 
     *      AJDT 1.2.0.20050308091611 although I activate the warning
     *      via the project properties.
     *      I see it only when I start the compiler from the commandline
     *      (ajc -XlazyTjp -Xlint:warning -inpath src -d classes src/x/...)
     */

    public int Car.breakSerial = 1;

    
    
    /*
     * example for "noInterfaceCtorJoinpoint"
     */
    
    pointcut interfaceConstructor() :
        execution(java.util.List.new());
    
}

class Car implements Serializable {}


public class Main extends Object {

    /**
     * @param args
     */
    public static void main(String[] args) {
        new Main().run();
        Long l = new Long(1);
        String s = l.toString();
    }
    
    public void run() {
        new Car().toString();
    }

}
