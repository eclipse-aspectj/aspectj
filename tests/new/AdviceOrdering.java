import org.aspectj.testing.*;

public class AdviceOrdering {
    public static void main(String[] args) { test(); }
    
    public static void test() {
        new C().m();
        T.checkAndReset("before:aroundStart:m:aroundEnd:after:afterReturning");

        new C().recur(2);
        T.checkAndReset("P&&!cflowbelow(P):P:recur-2:P:recur-1:P:recur-0");

        new C().a();
        T.checkAndReset("C1:C2:C3:C4:A3:A4:A1:A2:B1:a");
    }
}


class T {
    private static StringBuffer order = new StringBuffer();
    public static void add(String s) { 
        if (order.length() > 0) { order.append(':'); }
        order.append(s);
    }
    public static void reset() { order = new StringBuffer(); }

    public static void checkAndReset(String expectedValue) {
        Tester.checkEqual(order.toString(), expectedValue);
        order.setLength(0);
    }
}

class C {
    public void m() { T.add("m"); }
    public void a() { T.add("a"); }
    public void b() { T.add("b"); }
    public void c() { T.add("c"); }

    public void recur(int n) {
        T.add("recur-"+n);
        if (n > 0) recur(n-1);
    }
}

aspect A {
    pointcut cut() : target(C) && call(void m());

    before():            cut() { T.add("before"); }
    void around(): cut() {
        T.add("aroundStart");
        proceed();
        T.add("aroundEnd");
    }

    after():             cut() { T.add("after"); }
    after() returning(): cut() { T.add("afterReturning"); }

}

//check that P && !cflow(P) never matches anything regardless of ordering issues 
aspect FlowCheck {
    pointcut cut() : target(C) && call(void recur(int));
    
    before(): cut() && !cflow(cut()) {
        // should never run
        T.add("P&&!cflow(P)");
    }
    before(): cut() && !cflowbelow(cut()) {
        // should run once
        T.add("P&&!cflowbelow(P)");
    }

    before(): cut() && cflow(cut() && !cflow(cut())) {
        // should never run
        T.add("cflow(P&&!cflow(P))");
    }

    before(): cut() {
        T.add("P");
    }
}



// This cluster of aspects checks that the partial order rules work
//aspect A1 dominates A2, B1 {
aspect A1 { declare precedence: A1, A2 || B1;
    pointcut cut() : target(C) && call(void a());

    before(): A1.cut() { T.add("A1"); }
}

aspect A2 { declare precedence: A2, B1;
    before(): A1.cut() { T.add("A2"); }
}

aspect A3 { declare precedence: A3,  A4, A1;
    before(): A1.cut() { T.add("A3"); }
}

aspect A4 { declare precedence: A4, A1;
    before(): A1.cut() { T.add("A4"); }
}

aspect B1 {
    before(): A1.cut() { T.add("B1"); }
}



//aspect C1 dominates C2, C3 {
aspect C1 { declare precedence: C1, C2 || C3;
    before(): A1.cut() { T.add("C1"); }
}
aspect C2 { declare precedence: C2, C3;
    before(): A1.cut() { T.add("C2"); }
}
aspect C3 { declare precedence: C3, C4;
    before(): A1.cut() { T.add("C3"); }
}
//aspect C4 dominates A1, A2, A3, A4, B1 {
aspect C4 { declare precedence: C4, (A1 || A2 || A3 || A4 || B1);
    before(): A1.cut() { T.add("C4"); }
}

