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

import org.aspectj.lang.*;

/** test cases for controlling field writes */
public class RuntimeWrites {

    public static void main(String[] args) {
        System.err.println("---- setup: valid write");
        final SubPrinterStream me = new SubPrinterStream();

        System.err.println("---- setup: invalid write, outside initialization - nonstatic");
        me.setOne(1);

        System.err.println("---- setup: invalid write, outside initialization - static");
        me.one = 0;

        System.err.println("---- setup: invalid write, caller is not same as target");
        PrinterStream other = new PrinterStream(me);
    }
}


/** 
 * Control field writes.
 * This implementation restricts writes to the same object during initialization.
 * This is like having field values be final, except that 
 * they may be set set outside the constructor.
 * @author Wes Isberg
 */
aspect ControlFieldWrites {
    public static boolean throwError;
    
    // article page 42 - field writes

    // START-SAMPLE testing-inoculated-permitWritesDuringConstruction Constructor execution
    /** execution of any constructor for PrinterStream */
    pointcut init() : execution(PrinterStream+.new(..));
    // END-SAMPLE testing-inoculated-permitWritesDuringConstruction

    // START-SAMPLE testing-inoculated-prohibitWritesExceptWhenConstructing Prohibit field writes after construction
    /** any write to a non-static field in PrinterStream itself */
    pointcut fieldWrites() : set(!static * PrinterStream.*);


    /** 
     * Handle any situation where fields are written
     * outside of the control flow of initialization
     */
    before() : fieldWrites() && !cflow(init()) { 
        handle("field set outside of init", thisJoinPointStaticPart);
    }
    // END-SAMPLE testing-inoculated-prohibitWritesExceptWhenConstructing

    // START-SAMPLE testing-inoculated-prohibitWritesByOthers Prohibit field writes by other instances
    /** 
     * Handle any situation where fields are written
     * by another object.
     */
    before(Object caller, PrinterStream targ) : this(caller) 
        && target(targ) && fieldWrites() {
        if (caller != targ) {
            String err = "variation 1: caller (" + caller 
                + ") setting fields in targ (" + targ + ")";
            handle(err, thisJoinPointStaticPart);
        }
    }
    // END-SAMPLE testing-inoculated-prohibitWritesByOthers

    //---- variations to pick out subclasses as well
    // START-SAMPLE testing-inoculated-prohibitWritesEvenBySubclasses Prohibit writes by subclasses
    /** any write to a non-static field in PrinterStream or any subclasses */
    //pointcut fieldWrites() : set(!static * PrinterStream+.*);

    /** execution of any constructor for PrinterStream or any subclasses */
    //pointcut init() : execution(PrinterStream+.new(..));
    // END-SAMPLE testing-inoculated-prohibitWritesEvenBySubclasses

    //---- variation to pick out static callers as well
    // START-SAMPLE testing-inoculated-prohibitWritesEvenByStaticOthers Prohibit writes by other instances and static methods
    /** 
     * Handle any situation where fields are written
     * other than by the same object.
     */
    before(PrinterStream targ) : target(targ) && fieldWrites() {
        Object caller = thisJoinPoint.getThis();
        if (targ != caller) {
            String err = "variation 2: caller (" + caller 
                + ") setting fields in targ (" + targ + ")";
            handle(err, thisJoinPointStaticPart);
        }
    }
    // END-SAMPLE testing-inoculated-prohibitWritesEvenByStaticOthers

    //-------------- utility method
    void handle(String error, JoinPoint.StaticPart jpsp) {
        error += " - " + jpsp;
        if (throwError) {
            throw new Error(error);
        } else {
            System.err.println(error);
        }
    }
}


class PrinterStream {
    int one;
    private int another;

    PrinterStream() { setOne(1); }

    PrinterStream(PrinterStream other) { 
        other.another = 3;
    }

    public void setOne(int i) { 
        one = i; 
    }
}

class SubPrinterStream extends PrinterStream {
    private int two;

    SubPrinterStream() { 
        setOne(2); 
        setTwo(); 
    }

    public void setTwo() { 
        two = 2; 
    }
}

