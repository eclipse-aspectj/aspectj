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
    
import java.io.*;

/** 
 * Demonstrate technique of fault-injection 
 * as coordinated by test driver.
 * @author Wes Isberg
 */
aspect InjectingIOException { 
    
    // article page 43 - fault injection
    // START-SAMPLE testing-inoculated-injectIOException Inject IOException on test driver command
    /** the test starts when the driver starts executing */
    pointcut testEntryPoint(TestDriver driver) :
        target(driver) && execution(* TestDriver.startTest());

    /** 
     * The fault may be injected at the execution of any
     * (non-static) PrinterStream  method that throws an IOException
     */
    pointcut testCheckPoint(PrinterStream stream) : target(stream) 
        && execution(public * PrinterStream+.*(..) throws IOException);

    /**
     * After the method returns normally, query the
     * test driver to see if we should instead throw
     * an exception ("inject" the fault).
     */
    after (TestDriver driver, PrinterStream stream) returning 
            throws IOException :
            cflowbelow(testEntryPoint(driver))
            && testCheckPoint(stream) {
        IOException e = driver.createException(stream);
        if (null != e) {
            System.out.println("InjectingIOException - injecting " + e);
            throw e;
        }
    }
    /* Comment on the after advice IOException declaration:

       "throws IOException" is a declaration of the advice,
       not the pointcut.

       Since the advice might throw the injected fault, it
       must declare that it throws IOException. When advice declares
       exceptions thrown, the compiler will emit an error if any 
       join point is not also declared to throw an IOException.  

       In this case, the testCheckPoint pointcut only picks out 
       methods that throw IOException, so the compile will not
       signal any errors.
    */
    // END-SAMPLE testing-inoculated-injectIOException
}

/** this runs the test case */
public class Injection {
    /** Run three print jobs, two as a test and one normally */
    public static void main(String[] args) throws Exception {
        Runnable r = new Runnable() {
                public void run() { 
                    try { new TestDriver().startTest(); } 
                    catch (IOException e) {
                        System.err.println("got expected injected error " + e.getMessage());
                    }
                }
            };

        System.out.println("Injection.main() - starting separate test thread");
        Thread t = new Thread(r);
        t.start();

        System.out.println("Injection.main() - running test in this thread");
        r.run();
        t.join();

        System.out.println("Injection.main() - running job normally, not by TestDriver");
        new PrintJob().runPrintJob();
    }
}

/** handle starting of test and determining whether to inject failure */
class TestDriver {

    /** start a new test */
    public void startTest() throws IOException {
        new PrintJob().runPrintJob();
    }

    /** this implementation always injects a failure */
    public IOException createException(PrinterStream p) {
        return new IOException(""+p);
    }
}

//--------------------------------------- target classes 

class PrintJob {
    /** this job writes to the printer stream */
    void runPrintJob() throws IOException {
        new PrinterStream().write();
    }
}

class PrinterStream {
    /** this printer stream writes without exception */
    public void write() throws IOException {
        System.err.println("PrinterStream.write() - not throwing exception");
    }
}

