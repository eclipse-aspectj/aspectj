
package declares;

import com.company.app.*;
import java.rmi.RemoteException;
import java.io.IOException;

/**
 * @author Wes Isberg
 */
aspect A {
    
    // START-SAMPLE declares-threadFactoryRequired Error when not using Thread factory 
    /** signal error if Thread constructor called outside our Thread factory */
    declare error : call(Thread+.new(..)) && within(com.company..*)
        && !withincode(Thread com.company.lib.Factory.makeThread(..)) :
        "constructing threads prohibited - use Factory.makeThread(..)";
    // END-SAMPLE declares-threadFactoryRequired
    
}

// XXX platform invariants: J2ME, J2EE, AWT thread, ...

/* @author Wes Isberg */

aspect TypeConstraints {

// START-SAMPLE declares-typeConstraints Using declare to enforce type constraints
protected interface SoughtException {}
// XXX ajc broken here?
/**
 * Require that any SoughtException implementation be 
 * a subclass of Throwable.  This picks out the mistake
 * of declaring SoughtException a parent of something
 * that is not an exception at all.
 */
declare error : staticinitialization(SoughtException+) 
    && ! staticinitialization(SoughtException)    
    && ! staticinitialization(Throwable+) :
    "all SoughtException must be subclasses of Throwable";
//  END-SAMPLE declares-typeConstraints 
}

// START-SAMPLE declares-exceptionSpelunking Using declare warning to find Exception-related code

/**
 * List AppException catch blocks and callers as a way 
 * of investigating a possibly-large code base.
 */
aspect SeekAppExceptions {
    pointcut withinScope() : within(com.company..*);
      
    /**
     * Find calls to stuff that throws AppException.
     */
    declare warning : withinScope() && 
        (call(* *(..) throws AppException+)
         || call(new(..) throws AppException+)) :
        "fyi, another call to something that can throw IOException";

    /**
     * Find catch clauses handling AppException
     */
    declare warning : withinScope() && handler(AppException+):
        "fyi, code that handles AppException";
}
// END-SAMPLE declares-exceptionSpelunking 


/** @author Jim Hugunin, Wes Isberg */

class RuntimeRemoteException extends RuntimeException {
    RuntimeRemoteException(RemoteException e) {}
}

// XXX untested sample declares-softenRemoteException

// START-SAMPLE declares-softenRemoteException

/**
 * Convert RemoteExceptions to RuntimeRemoteException 
 * and log them. Enable clients that don't handle
 * RemoteException. 
 */
aspect HandleRemoteException {
    /**
     * Declare RemoteException soft to enable use by clients 
     * that are not declared to handle RemoteException. 
     */
    declare soft: RemoteException: throwsRemoteException();

    /**
     * Pick out join points to convert RemoteException to 
     * RuntimeRemoteException.
     * This implementation picks out
     * execution of any method declared to throw RemoteException
     * in our library.
     */
    pointcut throwsRemoteException(): within(com.company.lib..*)
       && execution(* *(..) throws RemoteException+);

    /** 
     * This around advice converts RemoteException to
     * RuntimeRemoteException at all join points picked out
     * by <code>throwsRemoteException()</code>.
     * That means *no* RemoteException will be thrown from
     * this join point, and thus that none will be converted
     * by the AspectJ runtime to <code>SoftException</code>.
     */
    Object around(): throwsRemoteException() {
        try {
            return proceed();
        } catch (RemoteException re) {
            re.printStackTrace(System.err);
            throw new RuntimeRemoteException(re);
        }
    }
}        
//END-SAMPLE declares-softenRemoteException

/*
  XXX another declare-soft example from Jim:
  
aspect A {
  
pointcut check():
    within(com.foo.framework.persistence.*) &&
    executions(* *(..));

declare soft: SQLException: check();

after () throwing (SQLException sqlex): check() {
  if (sql.getSQLCode().equals("SZ001")) {
    throw new AppRuntimeException("Non-fatal Database error occurred.",
                                  "cache refresh failure", sqlex);
  } else {
    throw new AppFatalRuntimeException(
                 "Database error occurred - contact support", sqlex);
  }
}
}
*/
