
package tracing;

import org.aspectj.lang.Signature;

/**
 * @author Wes Isberg
 */
aspect A {
    // START-SAMPLE tracing-simpleTiming Record time to execute public methods
    /** record time to execute my public methods */
    Object around() : execution(public * com.company..*.* (..)) {
        long start = System.currentTimeMillis();
        try {
            return proceed();
        } finally {
            long end = System.currentTimeMillis();
            recordTime(start, end, 
                thisJoinPointStaticPart.getSignature());
        }
    }
    // implement recordTime...
    // END-SAMPLE tracing-simpleTiming
    
    void recordTime(long start, long end, Signature sig) {
        // to implement...
    }
}
