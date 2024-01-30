

// START-SAMPLE tracing-traceJoinPoints   Trace to log join points executed by main method  
/* TraceMyJoinPoints.java */

package tracing;

import com.company.app.Main;

/**
 * Trace all join points in company application.
 * @author Jim Hugunin, Wes Isberg
 */
aspect TraceMyJoinPoints extends TraceJoinPoints {
    protected pointcut entry() : execution(void Main.runMain(String[]));
}
// END-SAMPLE tracing-traceJoinPoints        
