import org.aspectj.testing.*;

public class LocalInner {
    public static void main(String[] args) {
        class Local implements Runnable {
            public void run() {}
        }
        Local local = new Local();
        local.run();
        Tester.checkAllEvents();
    }
}

aspect Aspect {
    pointcut local(): callsto(receptions(void run()) && instanceof(Runnable));
    static before(): local()              { Tester.event("before-run"); }
    static after(): local()               { Tester.event("after-run"); }
    static around() returns void: local() { Tester.event("around-run"); proceed(); }
}
