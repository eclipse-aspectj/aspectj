import org.aspectj.testing.Tester;
import java.util.*;


public class HoldProceed {
    public static void main(String[] args) {
        C c = new C();
        c.m1(); c.m2(); c.m1();
        Tester.check("m");
        Tester.checkEqual(A.buf.toString(), "");
        A.runProceeds();
        Tester.checkEqual(A.buf.toString(), "m1:b:m2:m1:");
        A.runProceeds();
        Tester.checkEqual(A.buf.toString(), "m1:b:m2:m1:m1:b:m2:m1:");

        try {
            c.m3();
        } catch (Exception e) {
            Tester.check(false, "shouldn't throw anything");
        }

        try {
            A.runProceeds();
        } catch (Error err) {  //??? this is an odd test for the fact that Exception is wrapped
            Tester.note("caught err");
        }
        Tester.check("caught err");
    }
}

class C {
    public void m1() {A. buf.append("m1:"); }
    public void m2() {A. buf.append("m2:"); }
    public void m3() throws Exception { throw new Exception("from m3"); }
}

aspect A {
    static StringBuffer buf = new StringBuffer();

    static List heldProceeds = new LinkedList();

    int cnt = 0;

    void around (): call(void C.m*()) {
        heldProceeds.add(new Runnable() { public void run() { proceed(); cnt++; } });

        class MyRun implements Runnable {
            public void run() { System.out.println("run"); }
            public void m() { Tester.note("m"); } 
        }

        MyRun mr = new MyRun();
        mr.m();
    }

    before(): call(void C.m2()) {
        new Runnable() {
                public void run() { buf.append("b:"); }
                }.run();
    }
    
    public static void runProceeds() {
        for (Iterator i = heldProceeds.iterator(); i.hasNext(); ) {
            ((Runnable)i.next()).run();
        }
    }
}
