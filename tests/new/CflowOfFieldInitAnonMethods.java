
import org.aspectj.testing.Tester;

/** @testcase PR#755 ajc dies on cflow into field init anon class */
public class CflowOfFieldInitAnonMethods {
    public static void main(String[] args) {
        new CflowOfFieldInitAnonMethods().r.run(); // field initializer
        // no bug on static field initializers or with non-anonymous class
        // or when not calling another method 
        //XXX test should check, but that's for leter
        //Tester.checkAllEvents();
    }

    Runnable r = new Runnable() {
            public void run() { calc(1); }
            public void calc(int i) {}
        };
}

aspect ThreadTracer {
	pointcut safe(): !within(ThreadTracer);
	
    before(): safe() && cflow(call(void Runnable.run())) {
        Tester.event("before(): cflow(call(void Runnable.run()))");
    }
    before(): safe() && cflowbelow(call(void Runnable.run())) {
        Tester.event("before(): cflowbelow(call(void Runnable.run()))");
    }
    before(): safe() && cflow(execution(void Runnable.run())) {
        Tester.event("before(): cflow(execution(void Runnable.run()))");
    }
    before(): safe() && cflowbelow(execution(void Runnable.run())) {
        Tester.event("before(): cflowbelow(execution(void Runnable.run()))");
    }
    before(): execution(void Runnable.run()) { // no bug here
        Tester.event("before(): execution(void Runnable.run())");
    }
    static {
        Tester.expectEvent("before(): cflow(call(void Runnable.run()))");
        Tester.expectEvent("before(): cflowbelow(call(void Runnable.run()))");
        Tester.expectEvent("before(): cflow(execution(void Runnable.run()))");
        Tester.expectEvent("before(): cflowbelow(execution(void Runnable.run()))");
        Tester.expectEvent("before(): execution(void Runnable.run())");
    }
}

