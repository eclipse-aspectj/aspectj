
// START-SAMPLE tracing-traceJoinPoints Trace join points executed   
/* TraceJoinPointsBase.java */

package tracing;

import org.aspectj.lang.JoinPoint;

/**
 * Trace join points being executed in context.
 * To use this, define the abstract members in a subaspect.
 * <b>Warning</b>: this does not trace join points that do not
 * support after advice.
 * @author Jim Hugunin, Wes Isberg
 */
abstract aspect TraceJoinPointsBase {
    // this line is for AspectJ 1.1
    // for Aspectj 1.0, use "TraceJoinPointsBase dominates * {"
    declare precedence : TraceJoinPointsBase, *;

    abstract protected pointcut entry();

    protected pointcut exit(): call(* java..*.*(..));

    final pointcut start(): entry() && !cflowbelow(entry());

    final pointcut trace(): cflow(entry()) 
        && !cflowbelow(exit()) && !within(TraceJoinPointsBase+);

    private pointcut supportsAfterAdvice() : !handler(*)
        && !preinitialization(new(..));

    before(): start() { startLog(); }

    before(): trace() && supportsAfterAdvice(){ 
        logEnter(thisJoinPointStaticPart); 
    }

    after(): trace() && supportsAfterAdvice() { 
        logExit(thisJoinPointStaticPart); 
    }

    after(): start() { completeLog(); }
    
    abstract protected void logEnter(JoinPoint.StaticPart jp);
    abstract protected void logExit(JoinPoint.StaticPart jp);
    abstract protected void startLog();
    abstract protected void completeLog();
}

// END-SAMPLE tracing-traceJoinPoints        

          