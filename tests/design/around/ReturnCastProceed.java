import org.aspectj.testing.Tester;


public class ReturnCastProceed extends Helper {

    public static void main(String[] args) {
        StackChecker.setBaseDepth();

        Tester.checkEqual(mInt(), 3, "mInt is 3");
        Tester.checkAndClearEvents( new String[] { "advice" } );

        mVoid();
        Tester.checkAndClearEvents(new String[] {"advice", "void body" });

        Tester.checkEqual(mString(), "I'm a string", "mString is right");
        Tester.checkAndClearEvents(new String[] { "advice" });

        Tester.checkEqual(mInteger().intValue(), 5555, "mInteger is boxed 5555");
        Tester.checkAndClearEvents(new String[] { "advice" });

        Tester.check(mRunnable() instanceof Runnable, "mRunnable returns a Runnable");
        Tester.checkAndClearEvents(new String[] { "advice" });

        Tester.check(mObject() == f, "mObject returns f");
        Tester.checkAndClearEvents(new String[] { "advice" });

    }
}

class Helper {
    static Float f = new Float(37.8);

    static int mInt() {
        //StackChecker.checkDepth(2, "mInt");
        return 3;
    }

    static void mVoid() {
        StackChecker.checkDepth(2, "mVoid");
        Tester.event("void body");
    }

    static String mString() {
        StackChecker.checkDepth(2, "mString");
        return "I'm a string"; 
    }
    static Integer mInteger() {
        StackChecker.checkDepth(2, "mInteger");
        return new Integer(5555);
    }

    static Runnable mRunnable() {
        //StackChecker.checkDepth(2, "mRunnable");
        return new Runnable() {
                public void run() {
                    Tester.event("i'm running"); // not used yet.
                }
            };
    }

    static Object mObject() {
        StackChecker.checkDepth(2, "mObject");
        return f;
    }
}

aspect A {
     Object around(): execution(static * Helper.*(..)) { 
         Tester.event("advice");
         return (Object) proceed();
     }

//      Object around(): execution(static * ReturnCastProceed.*(..)) {  
//          Object ret = proceed();
//          System.err.println("doing stuff");
//          return ret;
//      }

//     Object around(): execution(static * ReturnCastProceed.*(..)) {
//         return proceed();
//     }

}    
