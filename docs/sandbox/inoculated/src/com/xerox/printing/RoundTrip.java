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

public class RoundTrip {
    public static void main(String[] args) {
        PrinterStream p = testing();
        System.err.println(" got "+ p.number);
    } 
    static PrinterStream testing() { return new PrinterStream(1); }

} 

/** @author Wes Isberg */
aspect VerifyPrinterStreamIntegrity {
    // article page 76 - round trip
    // START-SAMPLE testing-inoculated-roundTrip Round-trip integration testing 
    /**
     * After returning a PrinterStream from any call in our
     * packages, verify it by doing a round-trip between
     * PrinterStream and BufferedPrinterStream.
     * This uses a round-trip as a way to verify the
     * integrity of PrinterStream, but one could also use
     * a self-test (built-in or otherwise) coded specifically 
     * for validating the object (without changing state).
     */
    after () returning (PrinterStream stream) :
        call (PrinterStream+ com.xerox.printing..*(..)) 
        && !call (PrinterStream PrinterStream.make(BufferedPrinterStream)) {
        BufferedPrinterStream bufferStream = new BufferedPrinterStream(stream);
        PrinterStream newStream = PrinterStream.make(bufferStream);
        if (!stream.equals(newStream)) {
            throw new Error("round-trip failed for " + stream);
        } else {
            System.err.println("round-trip passed for " + stream);
        }
    }  
    // END-SAMPLE testing-inoculated-roundTrip
}

class BufferedPrinterStream {
    int num;
    BufferedPrinterStream(int i) { this.num = i; }
    BufferedPrinterStream(PrinterStream p) { this(p.number); }
}

class PrinterStream {
    int number;
    static PrinterStream make(BufferedPrinterStream p) { 
        return new PrinterStream(p.num); 
    }
    PrinterStream(int i) { this.number = i; }
    void write() {}
    // XXX hashcode
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (null == o) {
            return false;
        } else if (o instanceof PrinterStream) {
            return ((PrinterStream)o).number == number;
        } else {
            return o.equals(this);
        }
    }
}
