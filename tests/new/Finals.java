import org.aspectj.testing.*;
public class Finals {
    public static void main(String[] args) {
        new Finals().go(args);
        Tester.checkAllEvents();
    }

    static {
        Tester.expectEventsInString("go,i1,i2");
    }

    void go(String[] args) {
        Tester.event("go");
    }
}

interface I {
    public void i();
}
aspect Aspect {
    pointcut p1(): call(void go(..)) && target(Finals);
    void around(): p1() {
        new I() {
                public void i() {
                    a("i1");
                    proceed();
                }
            }.i();
    }

    pointcut p2(String[] argss): call(void go(String[])) && args(argss) && target(Finals);
    void around(final String[] argss): p2(argss) {
        new I() {
                public void i() {
                    a("i2");
                    proceed(argss);
                }
            }.i();
    }
    
    static void a(String s) { Tester.event(s); }
}
