/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg       initial implementation 
 * ******************************************************************/

// START-SAMPLE tracing-traceJoinPoints Trace join points executed   

package org.aspectj.lib.tracing;

import org.aspectj.lang.JoinPoint;

/**
 * Trace join points being executed in context.
 * To use this, define the abstract members in a subaspect.
 * <b>Warning</b>: this does not trace join points that do not
 * support after advice.
 * @author Jim Hugunin, Wes Isberg
 */
abstract aspect TraceJoinPointsBase {

    declare precedence : TraceJoinPointsBase, *;

    abstract protected pointcut entry();

    /** ignore join points outside this scope - use within(..) */
    abstract protected pointcut withinScope();

    protected pointcut exit(): withinScope() && call(* java..*.*(..));

    final pointcut start(): withinScope() && entry() && !cflowbelow(entry());

    final pointcut trace(): withinScope() && cflow(entry()) 
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
    
    /** called before any logging */
    abstract protected void startLog();
    
    /** called after any logging */
    abstract protected void completeLog();
}

// END-SAMPLE tracing-traceJoinPoints        

          