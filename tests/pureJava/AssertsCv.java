import org.aspectj.testing.Tester;

public class AssertsCv {
    public static void main(String[] args) {
        // programmatic enablement does not work after class is initialized
        AssertsCv.class.getClassLoader().setClassAssertionStatus("AssertTest", true);
        warnGlobalAssertions();
        AssertTest.main(args);
    }
    static void warnGlobalAssertions() {
        boolean pass = true;
        try { assert false; }
        catch (AssertionError e) { pass = false; }
        finally {
            if (!pass) {
                System.err.println("WARNING: assertions enabled in AssertsCv");
            }
        }
    }
}

class AssertTest {
    int x;
    public static void main(String[] args) {
        requireAssertions();
        testExpressions();
        testContexts();
    }

    static void testContexts() {
        InnerTester.testExpressions();
        (new AssertTest()).new InnerInstanceTester().testExpressions();

        // static initialization
        boolean pass = false;
        try { String s = InnerStaticInitFailure.class.getName(); }
        catch (AssertionError e) { pass = true; }
        catch (ExceptionInInitializerError e) {
            Throwable t = e.getException(); // use getCause() in 1.4
            pass = (null != t) && (t instanceof AssertionError);
        }
        finally { Tester.check(pass, "no assertion in static initializer"); }

        AssertTest enclosing = new AssertTest();

        // field initialization of inner class
        pass = false;
        try { enclosing.new InnerFieldInitFailure(); }
        catch (AssertionError e) { pass = true; }
        finally { Tester.check(pass, "no assertion in field initializer"); }

        // instance initialization of inner class
        pass = false;
        try { enclosing.new InnerInitFailure(); }
        catch (AssertionError e) { pass = true; }
        finally { Tester.check(pass, "no assertion in constructor"); }

        // anonymous inner class - method
        pass = false;
        boolean doingRun = false;
        try {
            enclosing.x = 1;
            final int i = enclosing.x;
            Runnable r = new Runnable() {
                    public void run() {
                        assert i != 1;
                    }
                };
            doingRun = true;
            r.run();
        } catch (AssertionError e) { pass = true; }
        finally {
            Tester.check(doingRun, "premature assertion in anonymous inner");
            Tester.check(pass, "no assertion in anonymous inner");
        }
        enclosing.x = 0;

        if (false) { // disabled now as current javac/j2se bug XXX
            // anonymous inner class - field initializer
            pass = false;
            try {
                enclosing.x = 1;
                Runnable r = new Runnable() {
                        int j = throwAssert();
                        public void run() { }
                        int throwAssert() { assert false; return -100; }
                    };
                doingRun = true;
                r.run();
            }
            catch (AssertionError e) { pass = true; }
            finally {
                Tester.check(!doingRun, "missed assertion in anonymous inner");
                Tester.check(pass, "no assertion in anonymous inner");
            }
            enclosing.x = 0;
        }
    }

    static void requireAssertions() {
        boolean pass = false;
        try { assert false; }
        catch (AssertionError e) { pass = true; }
        finally { Tester.check(pass, "assertions not enabled"); }
    }

    static void testExpressions() {
        // ---------- tests of not throwing assertions
        // first is any boolean expression
        int x = 0;
        assert x < 2;
        assert  // parser questions
            x<2
            ;
        assert (x<2);
        assert x < 2 ? true : false ;
        assert x < 2 ? true : false : false ;
        assert x < 2 ? true : false : "bug if assert thrown" ;
        // If the first expression evaluates to true,
        // the second expression is not evaluated
        assert x == 0 : MyError.throwError("bug if error thrown");
        assert x < 2 ? true : false : MyError.throwError("bug if error thrown");

        // ---------- tests of throwing assertions
        // (if these fail, it may be Sun's bug...)
        // touch expression2 type bases
        try { assert x > 0 : 3; }
        catch (AssertionError e) { check(e, "3"); }
        try { assert x > 0 : 3f; }
        catch (AssertionError e) { check(e, "3"); }
        try { assert x > 0 : 3d; }
        catch (AssertionError e) { check(e, "3"); }
        try { assert x > 0 : 3l; }
        catch (AssertionError e) { check(e, "3"); }
        try { assert x > 0 : 'c'; }
        catch (AssertionError e) { check(e, "c"); }
        try { assert x > 0 : "String"; }
        catch (AssertionError e) { check(e, "String"); }
        try { assert x > 0 : new StringWrapper("String"); }
        catch (AssertionError e) { check(e, "String"); }
        try { assert x > 0 : result(true); }
        catch (AssertionError e) { check(e, "true"); }
        try { assert x > 0 : Boolean.FALSE ; }
        catch (AssertionError e) { check(e, "false"); }
        try { assert x > 0 : Boolean.FALSE ; }
        catch (AssertionError e) { check(e, "false"); }

        // If an exception is thrown while either expression is being evaluated,
        // the assert statement completes abruptly, throwing this exception
        try { assert MyError.throwError("throwit"); }
        catch (MyError e) { check(e, "throwit"); }
        catch (AssertionError e) { Tester.check(false, "expected throwit-1"); }

        try { assert x > 0 : MyError.throwError("throwit"); }
        catch (MyError e) { check(e, "throwit"); }
        catch (AssertionError e) { Tester.check(false, "expected throwit-2"); }

        try { assert MyInnerError.throwError("throwit"); }
        catch (MyInnerError e) { check(e, "throwit"); }
        catch (AssertionError e) { Tester.check(false, "expected throwit-1"); }

        try { assert x > 0 : MyInnerError.throwError("throwit"); }
        catch (MyInnerError e) { check(e, "throwit"); }
        catch (AssertionError e) { Tester.check(false, "expected throwit-2"); }
    }

    static void check(Error e, String prefix) {
        String m = e.getMessage();
        Tester.check(m.startsWith(prefix),
                     "expected " + m
                     + " to start with " + prefix);
    }

    static boolean result(boolean b ) {
        return b;
    }
    private static class MyInnerError extends Error {
        MyInnerError(String s) { super(s); }
        static boolean throwError(String s) {
            throw new MyInnerError(s);
        }
    }
    private static class InnerTester {
        // copy/paste from above
        static void testExpressions() {
            // ---------- tests of not throwing assertions
            // first is any boolean expression
            int x = 0;
            assert x < 2;
            assert  // parser questions
                x<2
                ;
            assert (x<2);
            assert x < 2 ? true : false ;
            assert x < 2 ? true : false : false ;
            assert x < 2 ? true : false : "bug if assert thrown" ;
            // If the first expression evaluates to true,
            // the second expression is not evaluated
            assert x == 0 : MyError.throwError("bug if error thrown");
            assert x < 2 ? true : false : MyError.throwError("bug if error thrown");

            // ---------- tests of throwing assertions
            // (if these fail, it may be Sun's bug...)
            // touch expression2 type bases
            try { assert x > 0 : 3; }
            catch (AssertionError e) { check(e, "3"); }
            try { assert x > 0 : 3f; }
            catch (AssertionError e) { check(e, "3"); }
            try { assert x > 0 : 3d; }
            catch (AssertionError e) { check(e, "3"); }
            try { assert x > 0 : 3l; }
            catch (AssertionError e) { check(e, "3"); }
            try { assert x > 0 : 'c'; }
            catch (AssertionError e) { check(e, "c"); }
            try { assert x > 0 : "String"; }
            catch (AssertionError e) { check(e, "String"); }
            try { assert x > 0 : new StringWrapper("String"); }
            catch (AssertionError e) { check(e, "String"); }
            try { assert x > 0 : result(true); }
            catch (AssertionError e) { check(e, "true"); }
            try { assert x > 0 : Boolean.FALSE ; }
            catch (AssertionError e) { check(e, "false"); }
            try { assert x > 0 : Boolean.FALSE ; }
            catch (AssertionError e) { check(e, "false"); }

            // If an exception is thrown while either expression is being evaluated,
            // the assert statement completes abruptly, throwing this exception
            try { assert MyError.throwError("throwit"); }
            catch (MyError e) { check(e, "throwit"); }
            catch (AssertionError e) { Tester.check(false, "expected throwit-1"); }

            try { assert x > 0 : MyError.throwError("throwit"); }
            catch (MyError e) { check(e, "throwit"); }
            catch (AssertionError e) { Tester.check(false, "expected throwit-2"); }

            try { assert MyInnerError.throwError("throwit"); }
            catch (MyInnerError e) { check(e, "throwit"); }
            catch (AssertionError e) { Tester.check(false, "expected throwit-1"); }

            try { assert x > 0 : MyInnerError.throwError("throwit"); }
            catch (MyInnerError e) { check(e, "throwit"); }
            catch (AssertionError e) { Tester.check(false, "expected throwit-2"); }
        }
    }
    private class InnerInstanceTester {
        // copy/paste from above, with exception of x variable
        void testExpressions() {
            // ---------- tests of not throwing assertions
            // first is any boolean expression
            x = 0; // variable from enclosing instance
            assert x < 2;
            assert  // parser questions
                x<2
                ;
            assert (x<2);
            assert x < 2 ? true : false ;
            assert x < 2 ? true : false : false ;
            assert x < 2 ? true : false : "bug if assert thrown" ;
            // If the first expression evaluates to true,
            // the second expression is not evaluated
            assert x == 0 : MyError.throwError("bug if error thrown");
            assert x < 2 ? true : false : MyError.throwError("bug if error thrown");

            // ---------- tests of throwing assertions
            // (if these fail, it may be Sun's bug...)
            // touch expression2 type bases
            try { assert x > 0 : 3; }
            catch (AssertionError e) { check(e, "3"); }
            try { assert x > 0 : 3f; }
            catch (AssertionError e) { check(e, "3"); }
            try { assert x > 0 : 3d; }
            catch (AssertionError e) { check(e, "3"); }
            try { assert x > 0 : 3l; }
            catch (AssertionError e) { check(e, "3"); }
            try { assert x > 0 : 'c'; }
            catch (AssertionError e) { check(e, "c"); }
            try { assert x > 0 : "String"; }
            catch (AssertionError e) { check(e, "String"); }
            try { assert x > 0 : new StringWrapper("String"); }
            catch (AssertionError e) { check(e, "String"); }
            try { assert x > 0 : result(true); }
            catch (AssertionError e) { check(e, "true"); }
            try { assert x > 0 : Boolean.FALSE ; }
            catch (AssertionError e) { check(e, "false"); }
            try { assert x > 0 : Boolean.FALSE ; }
            catch (AssertionError e) { check(e, "false"); }

            // If an exception is thrown while either expression is being evaluated,
            // the assert statement completes abruptly, throwing this exception
            try { assert MyError.throwError("throwit"); }
            catch (MyError e) { check(e, "throwit"); }
            catch (AssertionError e) { Tester.check(false, "expected throwit-1"); }

            try { assert x > 0 : MyError.throwError("throwit"); }
            catch (MyError e) { check(e, "throwit"); }
            catch (AssertionError e) { Tester.check(false, "expected throwit-2"); }

            try { assert MyInnerError.throwError("throwit"); }
            catch (MyInnerError e) { check(e, "throwit"); }
            catch (AssertionError e) { Tester.check(false, "expected throwit-1"); }

            try { assert x > 0 : MyInnerError.throwError("throwit"); }
            catch (MyInnerError e) { check(e, "throwit"); }
            catch (AssertionError e) { Tester.check(false, "expected throwit-2"); }
        }
    }

    static class InnerStaticInitFailure {
        static {
            int x = 0;
            assert x > 1 : "throwing assert during class init";
        }
    }

    class InnerFieldInitFailure {
        int i = getValue();
        int getValue () {
            int x = 0;
            assert x > 1 : "throwing assert during field init";
            return -100;
        }
    }

    class InnerInitFailure {
        InnerInitFailure() {
            int x = 0;
            assert x > 1 : "throwing assert during instance init";
        }
    }
}

class MyError extends Error {
    MyError(String s) { super(s); }
    static boolean throwError(String s) {
        throw new MyError(s);
    }
}

class StringWrapper {
    private String string;
    StringWrapper(String s) { string = s; }
    public String toString() { return string; }
}

