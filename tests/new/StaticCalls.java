import org.aspectj.testing.Tester;

public class  StaticCalls {
    public static void main(String args[]) { test(); }

    Object server = null;

    void run() {
        Tester.checkEqual(StaticCalls.lookup("TimeService0"),
                          "TimeService0",
                          "untouched");
        Tester.checkEqual(StaticCalls.lookup("InterceptThis"),
                          "FromAround",
                          "touched");
        Tester.checkEqual(this.lookup("InterceptThis"),
                          "FromAround",
                          "this and touched");
        Tester.checkEqual(lookup("InterceptThis"),
                          "FromAround",
                          "lexical and touched");
    }

    public static void test() {
        new StaticCalls().run();

        Class c = Class.forName("java.lang.Foo");
        Tester.check(c == null, "intercepted exception and returned null");
    }

    static String lookup(String s){
        return s;
    }
}

aspect Aspect {
    Object around(String s):
        within(StaticCalls) && call(String StaticCalls.lookup(String)) && args(s)
        {
            if (s.equals("InterceptThis")) return "FromAround";
            else return proceed(s);
        }

    pointcut classForName(): call(Class Class.forName(String));

    declare soft: ClassNotFoundException: classForName();

    Class around(): classForName() {
        try {
            return proceed();
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
