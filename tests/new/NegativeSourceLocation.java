import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;

import org.aspectj.testing.*;

/** 
 * @testcase PR#525 validate presence/absence of thisEnclosingJoinPointStaticPart  
 * @testcase PR#525 validate SourceLocation
 */
public class NegativeSourceLocation {

    public static void main(String[] args) {
        Signal.expect(Const.EXPECTED);
        TargetClass target = new TargetClass();
        // run from outside code the compiler controls
        Thread t = new Thread(target, "NegativeSourceLocation");
        t.start();
        int i = 0;
        // todo: use Timer to add interrupt?
        while ((10 > i++) && t.isAlive()) {
            try { t.join(); }
            catch (InterruptedException e) {}
        }
        Signal.checkAll();
    }
}

/** indirection for test utility */
class Signal {
    public static final void failed(String s) {
        //Thread.currentThread().dumpStack();
        Tester.checkFailed(s);
    }
    public static final void found(JoinPoint.StaticPart s) {
        Tester.event(s.toString());
    }
    public static final void found(String s) {
        Tester.event(s);
    }
    public static final void checkAll() {
        Tester.checkAllEvents();
    }
    public static final void expect(String[] sra) {
        Tester.expectEventsInString(sra);
    }
} // class Signal

/** TargetClass has most every join point, provoked by initializatin and run() */
class TargetClass implements Runnable {
     TargetClass() {}
     static String staticString = "two";     // jp has -1 source location
     static {
          staticString = "one";
          String s = staticString + "asdf";
     }
     static {
          staticString = "three";
     }
     String string = "bar"; 
     public static void staticRun() {        // execute
          staticString = "foo";              // set - static var
          String s = staticString;           // get - static var
          s = s + "ss";
          staticString = s + "u";            // set - instance var
          TargetClass u = new TargetClass(); // initialization
          staticString = u.toString();                 
     }
     public void run() {                     // execute - no thisEnclosingJoinPoint when called from Thread.start()
         boolean doNotOptimize = (staticString != null);
         if (doNotOptimize) internalRun();
     }
     private void internalRun() {            // execute
          staticString = "foo";              // set - static var
          staticRun();                       // call static
          String s = staticString;           // get - static var
          String t = string;                 // get - instance var
          s = s + t;
          string = s + t;                    // set - instance var
          final Error e = new Error("caught here");
          try {
                throw e;
          } catch (Error er) {               // handler
                if (er != e) {
                     Signal.failed("caught Error=" + er);
                } else {
                     Signal.found("caught");
                }
          }
     }
} // class TargetClass

/**
 * This Aspect attempts to specify join points that have enclosing
 * join points and whether the source locations are valid.
 * It fails in using only pointcuts. 
 * This is just a first cut.  I (wes) have an AroundAll
 * which does this tracking...
 */
aspect Aspect {

    // ------------------------------- pointcuts select logical sets of join points
    // 1.1 includes StringBuffer calls that weren't in 1.0
    pointcut allTargetJoinPoints() 
        : within(TargetClass) && 
        	!call(* StringBuffer.*(..)) && !call(StringBuffer.new(..))
        	&& !call(* String.valueOf(Object));

    /** these have no enclosing join point */
    pointcut noEnclosingJoinPoint() 
        : ((call(public void TargetClass.run()))   // called from Thread.start() (caller-side, but has callee-side?)
           //|| staticinitialization(TargetClass)    // the enclosing jp here is itself
           );
           // || initialization(TargetClass.new())   
           // || execution(TargetClass.new())        // todo: expect it to be self like in methods?

     
    /** these have enclosing join points */
    pointcut hasEnclosingJoinPoint() 
        : allTargetJoinPoints() 
        && !(noEnclosingJoinPoint()) 
        ;

    /** expecting an enclosing join point different from thisJoinPoint */
    pointcut enclosingDiffers() 
        :  get(* TargetClass.*)
            || set(* TargetClass.*)
            || call(* TargetClass.*(..))
            || handler(Error)
        ;

    pointcut hasDifferentEnclosingJoinPoint() 
        : hasEnclosingJoinPoint()
        && enclosingDiffers()
        ;

    pointcut hasSameEnclosingJoinPoint() 
        : hasEnclosingJoinPoint()
        && (!enclosingDiffers());

    /** synthetic join points have no source location */
    pointcut syntheticJoinPoints() 
        : staticinitialization(TargetClass) 
        || initialization(TargetClass.new(UnknownConstructor))
        ;

    pointcut joinPointHasValidSourceLocation()
        : allTargetJoinPoints() 
        && (!syntheticJoinPoints())
        //&& if(expectSourceLocation(thisJoinPointStaticPart))
        ;

    pointcut enclosingJoinPointHasValidSourceLocation() // todo: cannot specify
        : hasEnclosingJoinPoint() 
        && (!syntheticJoinPoints()) 
        //&& if(expectSourceLocation(thisEnclosingJoinPointStaticPart))
        ;

    // ---------------------- advice applies invariants to each logical set of join points
    /** @testcase all join points have non-null thisJoinPoint and thisJoinPointStaticPart */
    before(): allTargetJoinPoints() {
        Signal.found("before AllTargetJoinPoints "  + thisJoinPointStaticPart);
        //System.err.println(thisJoinPointStaticPart + " at " + thisJoinPointStaticPart.getSourceLocation());
        String test = "all join points have non-null thisJoinPointStaticPart";
        if (null == thisJoinPoint) {
            Signal.failed(test + " failed with null thisJoinPoint: " + thisJoinPointStaticPart);
        }
        if (null == thisJoinPointStaticPart) {
            Signal.failed(test + " failed with null thisJoinPointStaticPart: " + thisJoinPoint);
        }
    }

    /** @testcase non-null thisEnclosingStaticJoinPoint at certain join points */
    before() : hasEnclosingJoinPoint() {
        String test = "failed (most join points have non-null thisEnclosingStaticJoinPoint) ";
        if (null == thisEnclosingJoinPointStaticPart) {
            String jpName = thisJoinPointStaticPart.toString();
            Signal.failed(test + render(thisJoinPointStaticPart, thisEnclosingJoinPointStaticPart));
            //if (!jpName.equals("execution(TargetClass.<init>)")) { // todo: unable to specify this...
        }
    }

    /** @testcase non-null thisEnclosingStaticJoinPoint at join points (except for tested exceptions) */
    before() : hasDifferentEnclosingJoinPoint() {
        String test = "join points with different thisEnclosingStaticJoinPoint";
        if (thisEnclosingJoinPointStaticPart != thisEnclosingJoinPointStaticPart) {
            Signal.failed(test + " different static part : " + thisJoinPointStaticPart);
        }
    }

    /** @testcase expecting valid source locations */
    before() : joinPointHasValidSourceLocation() {
        if (null == thisJoinPointStaticPart) {
            Signal.failed("null thisJoinPointStaticPart");
        } else {
            checkSourceLocation(thisJoinPointStaticPart);
        }
    }

    /** @testcase expecting valid source locations in enclosing join point */
    before() : enclosingJoinPointHasValidSourceLocation() {
        if (null == thisEnclosingJoinPointStaticPart) {
            Signal.failed("null thisEnclosingJoinPointStaticPart in " + thisJoinPointStaticPart);
        } else {
            checkSourceLocation(thisEnclosingJoinPointStaticPart);
        }
    }

    /** @testcase non-null thisEnclosingJoinPointStaticPart in static initializer if invoked within a join point?  */
    before() : staticinitialization(AnotherTargetClass) {    
        String test = "static initializer join points have non-null thisJoinPointStaticPart when invoked from CCC";
        if (null == thisJoinPoint) {
            Signal.failed(test + " failed with null thisJoinPoint: " + thisJoinPointStaticPart);
        }
        if (null == thisJoinPointStaticPart) {
            Signal.failed(test + " failed with null thisJoinPointStaticPart: " + thisJoinPoint);
        }
        Signal.found("staticinitialization(AnotherTargetClass))");
        //Signal.found(thisJoinPointStaticPart); // todo: relying on formatting of toString() - fix
    }

    /** @testcase no call from outside CCC has thisEnclosingJoinPointStaticPart (possible mistake) */
    before() : noEnclosingJoinPoint() {
        Signal.found("before noEnclosingJoinPoint "  + thisJoinPointStaticPart);
        if (null != thisEnclosingJoinPointStaticPart) {
                Signal.failed("unexpected non-null thisEnclosingJoinPointStaticPart: "
                          + thisEnclosingJoinPointStaticPart + " from " + thisJoinPointStaticPart);
        }
    }

    static String render(JoinPoint.StaticPart jp, JoinPoint.StaticPart ejp) {
        StringBuffer sb = new StringBuffer();
        sb.append("thisJoinPoint: ");
        sb.append(null == jp ? "null" : jp.toString());
        sb.append("thisEnclosingJoinPoint: ");
        sb.append(null == ejp ? "null" : ejp.toString());
        return sb.toString();
    }

    void checkSourceLocation(JoinPoint.StaticPart jp) { // todo: provide caller context?
        checkSourceLocation(jp.getSourceLocation(), jp.toString());
    }

    /** aborted attempt to check jp by name for jp without enclosing */
    private static boolean expectSourceLocation(JoinPoint.StaticPart jp) {
        if (null == jp) {
            return false;
        } else {
            String name = jp.toString();
            if (-1 != name.indexOf("TargetClass.<init>")) {
                return false;
            }
        }
        return true; // todo: overinclusive
    }

    private boolean inInitCode(JoinPoint.StaticPart jp) {
        return (-1 != jp.toString().indexOf("<init>"));
    }

    void checkSourceLocation(SourceLocation sl, String context) {
        if (sl == null) {
            Signal.failed(context + "null SourceLocation");
        } else {
            int i = sl.getLine();
            if (0 > i) {
                Signal.failed(context + " line<0: " + i);
            }
            // 1.1 doesn't provide column info
//            i = sl.getColumn();
//            if (0 > i) {
//                Signal.failed(context + " column<0: " + i);
//            }
        }
    }
} // Aspect

/** more readable to put expected messages at end of file */
class Const {
    // todo: EXPECTED will break if JoinPoint.StaticPart.toString() changes
    public static final String[] EXPECTED = new String[] 
    {
          "before AllTargetJoinPoints staticinitialization(TargetClass.<clinit>)"
        , "before AllTargetJoinPoints set(String TargetClass.staticString)"
        , "before AllTargetJoinPoints get(String TargetClass.staticString)"
        , "before AllTargetJoinPoints get(String TargetClass.staticString)"
        , "before AllTargetJoinPoints set(String TargetClass.staticString)"
        , "before AllTargetJoinPoints set(String TargetClass.staticString)"
        , "before AllTargetJoinPoints preinitialization(TargetClass())"
        , "before AllTargetJoinPoints initialization(java.lang.Runnable())"
        //, "before AllTargetJoinPoints execution(java.lang.Runnable())"
        , "before AllTargetJoinPoints initialization(TargetClass())"
        //, "before AllTargetJoinPoints execution(TargetClass.<init>)"
        , "before AllTargetJoinPoints set(String TargetClass.string)"
        , "before AllTargetJoinPoints execution(TargetClass())"
        , "before AllTargetJoinPoints execution(void TargetClass.run())"
        , "before AllTargetJoinPoints call(void TargetClass.internalRun())"
        , "before AllTargetJoinPoints execution(void TargetClass.internalRun())"
        , "before AllTargetJoinPoints set(String TargetClass.staticString)"
        , "before AllTargetJoinPoints call(void TargetClass.staticRun())"
        , "before AllTargetJoinPoints execution(void TargetClass.staticRun())"
        , "before AllTargetJoinPoints set(String TargetClass.staticString)"
        , "before AllTargetJoinPoints get(String TargetClass.staticString)"
        , "before AllTargetJoinPoints set(String TargetClass.staticString)"
        , "before AllTargetJoinPoints call(TargetClass())"
        , "before AllTargetJoinPoints preinitialization(TargetClass())"        
        , "before AllTargetJoinPoints initialization(TargetClass())"
        , "before AllTargetJoinPoints initialization(java.lang.Runnable())"
        //, "before AllTargetJoinPoints execution(java.lang.Runnable())"
        //, "before AllTargetJoinPoints execution(TargetClass.<init>)"
        , "before AllTargetJoinPoints set(String TargetClass.string)"
        , "before AllTargetJoinPoints execution(TargetClass())"
        , "before AllTargetJoinPoints call(String java.lang.Object.toString())"
        , "before AllTargetJoinPoints set(String TargetClass.staticString)"
        , "before AllTargetJoinPoints get(String TargetClass.staticString)"
        , "before AllTargetJoinPoints get(String TargetClass.string)"
        , "before AllTargetJoinPoints set(String TargetClass.string)"
        , "before AllTargetJoinPoints call(java.lang.Error(String))"
        , "before AllTargetJoinPoints handler(catch(Error))"
        , "before AllTargetJoinPoints call(void Signal.found(String))"
        , "caught"
    };

}
