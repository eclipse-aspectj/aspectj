import org.aspectj.testing.Tester;

/** @testcase PR#774 interface self-reference by anonymous instance */
public class AnonymousSelfReference {
    public static void main (String[] args) {
        Tester.expectEvent("run");
        Tester.expectEvent("im");
        I it = new I() { public void im() { Tester.event("im");} };
        it.runnable.run();
        Tester.checkAllEvents();
    }
}

interface I { public void im(); }

aspect A {
    Runnable I.runnable = new Runnable() {
            public void run() { 
                im(); // comment out to avoid VerifyError
                Tester.event("run");
            } 
        };
}
