import org.aspectj.testing.Tester;

aspect Test {
        public static boolean invariant() { return true ; }
        after() returning : call(static void AssertInAdviceBug.call() ) {
            assert !invariant() ;
        }

        void around(): call(static void call1()) {
            assert !invariant();
        }

        static void AssertInAdviceBug.call2() {
            assert !invariant();
        }
}


/** @testcase PR#657 PUREJAVA assert statement in advice [requires 1.4] */
public class AssertInAdviceBug {
    private static boolean useJavacMode = false;

    public static void main(String[] args) {
        AssertInAdviceBug.class.getClassLoader().setClassAssertionStatus("Test", true);
        AssertInAdviceBug.class.getClassLoader().setClassAssertionStatus("AssertInAdviceBug", false);
        boolean gotAssert = false;
        try {
            call();
        } catch (AssertionError e) {
            gotAssert = true;
            StackTraceElement[] stack = e.getStackTrace();

            // this test should only run when we're not in -usejavac mode
            if (stack[0].getFileName().endsWith("AssertInAdviceBug.java")) {
                Tester.checkEqual(stack[0].getLineNumber(), 6, "bad line for assert");
            } else {
                useJavacMode = true;
                System.err.println("!!!!!!!!!!!!!!!!!!!!!!!IN JAVAC MODE!!!!!!!!!!!!!!!!!!!!1");
            }
        }
        Tester.check(gotAssert, "no assert");

        gotAssert = false;
        try {
            call1();
        } catch (AssertionError e) {
            gotAssert = true;
            StackTraceElement[] stack = e.getStackTrace();

            // this test should only run when we're not in -usejavac mode
            if (!useJavacMode) {
                Tester.checkEqual(stack[0].getLineNumber(), 10, "bad line for assert");
            }
        }
        Tester.check(gotAssert, "no assert on call1");

        gotAssert = false;
        try {
            call2();
        } catch (AssertionError e) {
            gotAssert = true;
            StackTraceElement[] stack = e.getStackTrace();
            //e.printStackTrace();

            // this test should only run when we're not in -usejavac mode
            if (!useJavacMode) {
                Tester.checkEqual(stack[0].getLineNumber(), 14, "bad line for assert");
            }
        }
        Tester.check(gotAssert, "no assert on call1");
    }


    public static void call() {}

    public static void call1() {}
}

