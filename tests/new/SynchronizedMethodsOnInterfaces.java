import org.aspectj.testing.*;
import java.lang.reflect.*;

public class SynchronizedMethodsOnInterfaces {
    public static void main(String[] args) {
        new SynchronizedMethodsOnInterfaces().realMain(args);
    }
    static {
        Tester.expectEventsInString("I,C");
    }
    public void realMain(String[] args) {
        method(new D());
        method(new C());
        Tester.checkAllEvents();
    }
    void method(Object o) {
        try {
            o.getClass().getMethod("method", new Class[]{}).invoke(o, new Object[]{});
        } catch (Throwable t) {
            Tester.check(false, t+"");
        }
    }
    
    public SynchronizedMethodsOnInterfaces() {
    }
}

interface I {}
class D implements I {}
class C {}

aspect AspectI {
    public synchronized void I.method() { Tester.event("I"); }
}
aspect AspectC {
    public synchronized void C.method() { Tester.event("C"); }
}
