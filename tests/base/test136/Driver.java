import org.aspectj.testing.Tester;

public class Driver {
    public static void main(String[] args) { test(); }

    public static void test() {
        C0 c = new C2();
        c.m1(2, 2);
        Tester.checkEqual(A.C0_m1_calls, 1, "C2.m1 -- C0 calls");
        Tester.checkEqual(A.C2_m1_calls, 1, "C2.m1 -- C2 calls");
        Tester.checkEqual(A.m1_afters, 1, "C2.m1 -- after advice");
        A.clearCounts();

        c.m2(c);
        Tester.checkEqual(A.C0_m1_calls, 1, "C2.m2 -- C0.m1 calls");
        Tester.checkEqual(A.C2_m1_calls, 0, "C2.m2 -- C2.m1 calls");
        Tester.checkEqual(A.m1_afters, 0, "C2.m2 -- after m1 advice");
        Tester.checkEqual(A.C0_m2_calls, 1, "C2.m2 -- C0.m2 calls");
        Tester.checkEqual(A.C2_m2_calls, 1, "C2.m2 -- C2.m2 calls");
        Tester.checkEqual(A.m2_afters, 1, "C2.m2 -- after m2 advice");

        c.m3("hi");
        Tester.checkEqual(A.C0_m3_calls, 1, "C2.m3 -- C0 calls");
        Tester.checkEqual(A.C1_m3_calls, 1, "C2.m3 -- C1 calls");
        Tester.checkEqual(A.C2_m3_calls, 1, "C2.m3 -- C2 calls");
    }
}

class C0 {
    public C0() {
    }

    void m1(int x, double y) {
        A.C0_m1_calls += 1;
    }

    void m2(C0 c0) {
        A.C0_m2_calls += 1;
    }
    void m3(String s) {
        A.C0_m3_calls += 1;
    }

}

class C1 extends C0 {
    void m3(String s) {
        super.m3(s);
        A.C1_m3_calls += 1;
    }
}

class C2 extends C1 {
    public boolean C2_after_called = false;
    
    public void setC2_after_called( boolean newVal ) { 
      C2_after_called = newVal;
    }
    
    public C2() {
        super();
    }

    void m1(int x, double y) {
        A.C2_m1_calls += 1;
        super.m1(x*2, x+y);
    }

    void m2(C0 c0) {
        A.C2_m2_calls += 1;
        super.m1(2, 2);
        super.m2(this);
    }
    void m3(String s) {
        super.m3(s);
        A.C2_m3_calls += 1;
    }
}

aspect A {
    public static int C0_m1_calls = 0;
    public static int C2_m1_calls = 0;
    public static int C0_m2_calls = 0;
    public static int C2_m2_calls = 0;
    public static int C0_m3_calls = 0;
    public static int C1_m3_calls = 0;
    public static int C2_m3_calls = 0;
    public static int m1_afters = 0;
    public static int m2_afters = 0;

    public static void clearCounts() {
        C0_m1_calls = 0;
        C2_m1_calls = 0;
        C0_m2_calls = 0;
        C2_m2_calls = 0;
        C0_m3_calls = 0;
        C1_m3_calls = 0;
        C2_m3_calls = 0;
        m1_afters = 0;
        m2_afters = 0;
    }

     after(): target(C0+) && call(void m1(int, double)) {
        m1_afters += 1;
    }
     after(): target(C0+) && call(* m2(..)) {
        m2_afters += 1;
    }

     after(): target(C0+) && call(* m3(..)) {
        int x = 22;
    }

     after(C2 c2) returning(): 
                    target(c2) && call(new()) {
        c2.setC2_after_called( true );
    }
}

