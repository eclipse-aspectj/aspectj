
import org.aspectj.testing.Tester;

/** @testcase PR#776 self-reference from (aspect-declared) method-local class */
public class MethodSelfReference {
    public static void main (String[] args) {
        I it = new I() { public void im() { } };
        it.start();
    }
}

interface I { public void im(); }

aspect A {
    Runnable I.runnable;
    void I.start() {
        class Runner implements Runnable {
            I ri;
            Runner(I i) { ri = i; }
            public void run() { ri.im(); }
        }
        runnable = new Runner(this);
        runnable.run();
    }
}

