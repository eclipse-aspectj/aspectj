
import org.aspectj.testing.Tester;
import org.aspectj.testing.Tester; 

/*
  These test advice on handlers:
  1) can use before advice to skip handler by throwing Error
  2) can use after advice after running handler and  throw  Error
  3) can use around advice [skip | run] X [ throw Error or complete normally ]
    a) skip without throwing error
    b) skip with throwing error
    c) run without throwing error
    d) run with throwing error

 Rather than overload advice at join points,
 there is one method and one advice for each of the 6 test cases.
 */
/** @testcase VerifyError after around advice falls off end of tryCatch */
public class AroundHandler {
    /** if true, then run the around test cases */
    public static final boolean TEST_AROUND = true ;  
    public static void main(String[] args) {
        Target target = new Target();
        /** @testcase before advice skips handler by throwing Error */
        Tester.check(target.skipBeforeErrorHandler(), "target.skipBeforeErrorHandler()");
        /** @testcase after advice runs handler, throws Error */
        Tester.check(target.runAfterErrorHandler(), "target.runAfterErrorHandler()");
        if (TEST_AROUND) {
            /** @testcase around advice skips handler, no Error thrown */
            Tester.check(target.skipErrorHandler(), "target.skipErrorHandler()");
            /** @testcase around advice runs handler, no Error thrown */
            Tester.check(target.runErrorHandler(), "target.runErrorHandler()");
            /** @testcase around advice skips handler, throws exception from around advice */
            Tester.expectEvent("skipErrorHandlerGotError");
            try {
                target.skipErrorHandlerThrowError();
                Tester.check(false, "expecting Error thrown by around");
            } catch (Error e) {
                Target.isERR(e);
                Tester.event("skipErrorHandlerGotError");
            }
            /** @testcase around advice runs handler, throws exception from around advice */
            Tester.expectEvent("runErrorHandlerThrowError");
            Tester.expectEvent("runErrorHandlerGotError");
            try {
                target.runErrorHandlerThrowError();
                Tester.check(false, "expecting Error thrown by around");
            } catch (Error e) {
                Target.isERR(e);
                Tester.event("runErrorHandlerGotError");
            }
        } // TEST_AROUND
    }
}
class OuterError extends Error {
    public OuterError(String s) { super(s); }
}
class Target {
    public static String ERR = "goto Error";
    public static void isERR(Throwable throwable) {
        String message = (null == throwable ? "" : throwable.getMessage());
        Tester.check(Target.ERR.equals(message),
                    "\"" + ERR + "\".equals(\"" + message + "\")");
    } 

    /** advised by before */
    public boolean skipBeforeErrorHandler() {
        boolean ranHandler = false;
        boolean ranOuterHandler = false;
        try {
            try { throw new Error(ERR); } 
            catch (Error t) { ranHandler = true; }
        } catch (OuterError t) { ranOuterHandler = true; }
        Tester.check(!ranHandler, "!ranHandler"); 
        Tester.check(ranOuterHandler, "ranOuterHandler"); 
        return (!ranHandler && ranOuterHandler);
    }

    /** advised by after */
    public boolean runAfterErrorHandler() {
        boolean ranHandler = false;
        boolean ranOuterHandler = false;
        try {
            try { throw new Error(ERR); } 
            catch (Error t) { ranHandler = true; }
        } catch (OuterError t) { ranOuterHandler = true; }
        Tester.check(ranHandler, "!ranHandler"); 
        Tester.check(ranOuterHandler, "ranOuterHandler"); 
        return (ranHandler && ranOuterHandler);
    }

    //---------------- remainder all advised using around 
    public boolean skipErrorHandler() {
        boolean ranHandler = false;
        try { throw new Error(ERR); } 
        catch (Error t) { ranHandler = true; }
        Tester.check(!ranHandler, "!ranHandler"); 
        return !ranHandler;
    }

    public boolean runErrorHandler() {
        boolean ranHandler = false;
        try { throw new Error(ERR); } 
        catch (Error t) { ranHandler = true; }
        Tester.check(ranHandler, "ranHandler"); 
        return ranHandler;
    }    

    public boolean skipErrorHandlerThrowError() {
        try { throw new Error(ERR); } 
        catch (Error t) { 
            Tester.check(false, "skipErrorHandlerThrowError ran handler"); 
        }
        return false; // should never get here - Error thrown
    }

    public boolean runErrorHandlerThrowError() {
        try { throw new Error(ERR); } 
        catch (Error t) { 
            Tester.event("runErrorHandlerThrowError");
        }
        return false; // should never get here - Error thrown
    }
}

aspect A {
    /** @testcase use before to skip handler by throwing Error from advice */
     before(Error error) 
        : withincode(boolean Target.skipBeforeErrorHandler()) 
        && handler(Error) && args(error) 
        {
        Target.isERR(error);
        throw new OuterError(Target.ERR);
    }

    /** @testcase use after to run handler but throw Error from advice */
     after(Error error) 
        : withincode(boolean Target.runAfterErrorHandler()) 
        && handler(Error) && args(error) 
        {
        Target.isERR(error);
        throw new OuterError(Target.ERR);
    }

    // -------------------- around advice

    /** @testcase use around, run handler */
    Object around(Error error) 
        : withincode(boolean Target.runErrorHandler())
        && handler(Error) && args(error) 
        && if(AroundHandler.TEST_AROUND)
        {
        Target.isERR(error);
        return proceed(error);
    }

    /** @testcase use around to skip handler, throw no Error from around */
    Object around(Error error) 
        : withincode(boolean Target.skipErrorHandler()) 
        && handler(Error) && args(error) 
        && if(AroundHandler.TEST_AROUND)
        {
        Target.isERR(error);
        //Object ignore = proceed(error);
        //throw new Error(Target.ERR);
        return null;
    }

    /** @testcase use around to skip handler, but throw Error from around */
    Object around(Error error) 
        : withincode(boolean Target.skipErrorHandlerThrowError()) 
        && handler(Error) && args(error) 
        && if(AroundHandler.TEST_AROUND)
        {
        Target.isERR(error);
        //Object ignore = proceed(error);
        throw new OuterError(Target.ERR);
        //return null;
    }

     /** @testcase use around, run handler, but throw Error from around */
    Object around(Error error) 
        : withincode(boolean Target.runErrorHandlerThrowError()) 
        && handler(Error) && args(error) 
        && if(AroundHandler.TEST_AROUND)
        {
        Target.isERR(error);
        Object result = proceed(error);
        throw new OuterError(Target.ERR);
        // return result;
    }
}
