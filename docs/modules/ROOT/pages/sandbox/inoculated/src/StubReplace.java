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

public class StubReplace {
  public static void main(String[] args) {
      new PrintJob().run();
  } 
} 

/** @author Wes Isberg */
aspect Stubs {

    // article page 76 - stubs

    // START-SAMPLE testing-inoculated-replaceWithProxy Replace object with proxy on constructiono
    /**
     * Replace all PrintStream with our StubStream
     * by replacing the call to any constructor of 
     * PrinterStream or any subclasses.
     */
    PrinterStream around () : within(PrintJob) 
        && call (PrinterStream+.new(..)) && !call (StubStream+.new(..)) {
        return new StubStream(thisJoinPoint.getArgs());
    }  
    // END-SAMPLE testing-inoculated-replaceWithProxy

    // START-SAMPLE testing-inoculated-adviseProxyCallsOnly Advise calls to the proxy object only
    pointcut stubWrite() : printerStreamTestCalls() && target(StubStream);

    pointcut printerStreamTestCalls() : call(* PrinterStream.write());

    before() : stubWrite() {
        System.err.println("picking out stubWrite" );
    }
    // END-SAMPLE testing-inoculated-adviseProxyCallsOnly
}

class PrinterStream {
    public void write() {}
}

class StubStream extends PrinterStream {
    public StubStream(Object[] args) {}
}

class PrintJob {
    public void run() {
        PrinterStream p = new PrinterStream();
        System.err.println("not PrinterStream: " + p);
        System.err.println("now trying call...");
        p.write();
    }
}

