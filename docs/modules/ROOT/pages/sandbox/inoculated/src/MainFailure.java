/*
 * Copyright (c) 1998-2002 PARC Inc.  All rights reserved.
 *
 * Use and copying of this software and preparation of derivative works based
 * upon this software are permitted.  Any distribution of this software or
 * derivative works must comply with all applicable United States export
 * control laws.
 *
 * This software is made available AS IS, and PARC Inc. makes no
 * warranty about the software, its performance or its conformity to any
 * specification.
 */

import java.util.*;
import java.io.*;
import org.aspectj.lang.*;

/** @author Wes Isberg */

public aspect MainFailure { 

    public static void main (String[] args) { TargetClass.main(args); }

    pointcut main(String[] args) :
        args(args) && execution(public static void main(String[]));

    // article page 42 - recording failures from main
    // START-SAMPLE testing-inoculated-failureCapture Log failures
    /** log after failure, but do not affect exception */
    after(String[] args) throwing (Throwable t) : main(args) {
        logFailureCase(args, t, thisJoinPoint);
    }
    // END-SAMPLE testing-inoculated-failureCapture

    // alternate to swallow exception
//      /** log after failure and swallow exception */
//      Object around() : main(String[]) {
//          try {
//              return proceed();
//          } catch (Error e) { // ignore
//              logFailureCase(args, t, thisJoinPoint);
//              // can log here instead
//          }
//          return null;
//      }

    public static void logFailureCase(String[] args, Throwable t, Object jp) {
        System.err.println("failure case: args " + Arrays.asList(args));
    }
}

class TargetClass {
    static Thread thread;
    /** will throw error if exactly one argument */
    public static void main (String[] args) {
        // make sure to do at least one failure
        if (thread == null) {
            Runnable r = new Runnable() {
                    public void run() {
                        main(new String[] {"throwError" });
                    }
                };
            thread = new Thread(r);
            thread.start();
        }
        if (1 == args.length) {
            throw new Error("hello");
        }
        try { thread.join(); }
        catch (InterruptedException ie) { }
    } 
}


