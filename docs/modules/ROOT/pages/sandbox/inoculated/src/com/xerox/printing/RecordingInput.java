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

package com.xerox.printing;

class PrinterBuffer {
    public int capacity(int i) { return i; }
}

public aspect RecordingInput {

    // @author Wes Isberg
    // article page 42 - recording input
    pointcut capacityCall (int i) :
        within(com.xerox..*) && args(i) 
        && call(public * PrinterBuffer.capacity(int)) ;
    // XXX style error - + not needed
    // call(public * PrinterBuffer+.capacity(int)) 

    before (int i) : capacityCall(i) {
        log.print("<capacityCall tjp=\"" + thisJoinPoint 
                  + "\" input=\"" + i + "\"/>");
    }

    Log log = new Log();
    class Log {
        void print(String s) { System.out.println(s); }
    }
    public static void main(String[] args) {
        PrinterBuffer p = new PrinterBuffer();
        p.capacity(1);
        p.capacity(2);
    } 
}
