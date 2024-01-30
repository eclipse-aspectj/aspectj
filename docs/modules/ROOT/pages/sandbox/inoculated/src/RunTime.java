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
import java.awt.Point;

import org.aspectj.lang.JoinPoint;

public class RunTime {
    public static void main(String[] a) {
        AnotherPoint.create();
        SubPoint.create();
    }
}

/** @author Wes Isberg */
aspect FactoryValidation {

    // START-SAMPLE declares-inoculated-prohibitNonprivateConstructors Error to have accessible sub-Point constructors
    /** We make it an error for any Point subclasses to have non-private constructors */
    declare error : execution(!private Point+.new(..)) 
        && !within(java*..*) :
        "non-private Point subclass constructor";
    // END-SAMPLE declares-inoculated-prohibitNonprivateConstructors

    // article page 41 - runtime NPE
    // START-SAMPLE testing-inoculated-runtimeErrorWhenNullReturnedFromFactory Throw Error when factory returns null
    /** Throw Error if a factory method for creating a Point returns null */
    after () returning (Point p) : 
        call(Point+ SubPoint+.create(..)) {
        if (null == p) {
            String err = "Null Point constructed when this (" 
                + thisJoinPoint.getThis() 
                + ") called target (" 
                + thisJoinPoint.getTarget() 
                + ") at join point (" 
                + thisJoinPoint.getSignature() 
                + ") from source location (" 
                + thisJoinPoint.getSourceLocation()
                + ") with args ("
                + Arrays.asList(thisJoinPoint.getArgs())
                + ")";
            throw new Error(err);
        }
    }
    // END-SAMPLE testing-inoculated-runtimeErrorWhenNullReturnedFromFactory
}

class SubPoint extends Point {
    public static SubPoint create() { return null; } // will cause Error
    private SubPoint(){}
}

class AnotherPoint extends Point {
    public static Point create() { return new Point(); }

    // to see that default constructor is picked out by declare error
    // comment out this constructor
    private AnotherPoint(){}
}


