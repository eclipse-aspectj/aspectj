import org.aspectj.testing.Tester;

// PR#262

import org.aspectj.testing.Tester;

public aspect Driver /*of eachobject(instanceof(CallsTo))*/ {

    public static void test() { main(null); }

    public static void main(String[] args) {
        CallsTo ct = new CallsTo();
        Tester.checkEqual(ct.a(), "s", "after calls");
    }

    pointcut call1(): call(* CallsFrom.b(..)) && within(CallsTo);
    before(): call1() {
        //System.out.println("before-call1");
    }
}

class CallsTo { public String a() { return new CallsFrom().b("s"); } }

class CallsFrom { public String b(String s) { return s; } }
