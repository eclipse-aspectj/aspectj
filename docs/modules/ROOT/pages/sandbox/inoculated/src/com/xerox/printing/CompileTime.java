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

import java.awt.Point;
import java.io.IOException;

class ClassOne {
    int i = 1 ; // 20 expect warning
}

class ClassError {
    int i = 1 ; // 24 expect warning
}

class PrinterStream {}

class SubPrinterStream extends PrinterStream {
    public void delegate() {
        try {
            throw new IOException("");
        } catch (IOException e) {}           // 33 expect error
    }
}

class SubPoint extends Point {
    Point create() { return new Point(); }  // no error
    Point another() { return new Point(); } // 39 expect error
}

/** @author Wes Isberg */
aspect CompileTime {

    // article page 40 - warning
    // START-SAMPLE declares-inoculated-nonSetterWrites Warn when setting non-public field
    /** warn if setting non-public field outside a setter */
    declare warning : 
        within(com.xerox.printing..*) 
        && set(!public * *) && !withincode(* set*(..)) 
        : "writing field outside setter" ;
    // END-SAMPLE declares-inoculated-nonSetterWrites

    // article page 41 - error
    // START-SAMPLE declares-inoculated-validExceptionHandlingMethod Error when subclass method handles exception
    declare error : handler(IOException+) 
            && withincode(* PrinterStream+.delegate(..))
        : "do not handle IOException in this method";
    // END-SAMPLE declares-inoculated-validExceptionHandlingMethod

    // START-SAMPLE declares-inoculated-validPointConstruction Error when factory not used
    declare error  : !withincode(Point+ SubPoint+.create(..)) 
            && within(com.xerox..*) 
            && call(Point+.new(..))
        : "use SubPoint.create() to create Point";
    // END-SAMPLE declares-inoculated-validPointConstruction
}


