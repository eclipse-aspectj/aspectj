import org.aspectj.testing.Tester;

public class MultiDispatchCf {
    public static void main(String[] args) {
        C c = new C();

        Tester.event("**** exec ****");
        //MultiExec.enabled = true;
        run(new C());

        Tester.event("**** call ****");
        //MultiExec.enabled = false;
        //MultiCall.enabled = true;
        run(new C());

        Tester.event("**** both=call ****");
        //MultiExec.enabled = true;
        run(new C());

        Tester.printEvents();
    }
    
    static void run(C c) {
        Tester.event(c.doit("s1"));
        Tester.event(c.doit(new Integer(10)));
        Tester.event(c.doit(new Double(1.25)));

        Object o;
        o = "s2"; Tester.event(c.doit(o));
        o = new Integer(20); Tester.event(c.doit(o));
        o = new Double(2.25); Tester.event(c.doit(o));
    }
}


class C {
    String doit(Object o) {
        return "did-" + o.toString();
    }
}

aspect MultiCall {
    pointcut t1(String s): call(String C.doit(Object)) && args(s);

    String around(String s): t1(s) { return proceed(s); }
    String around(Object o): t1(o) { return proceed(o); }



    pointcut m(Object o): call(String C.doit(Object)) && args(o);

    String getPrefix() { return "call"; }

    String around(String s): m(s) {  //ERR
        return getPrefix() + "-string-" + s;
    }
    String around(Integer i): m(i) {   //ERR
        return getPrefix() + "-integer-" + i;
    }
    String around(Double d): m(d) {    //ERR
        return getPrefix()  + "-double-" + d;
    }
}

aspect MultiCreate {
    pointcut make(Object o): this(o) && execution(new(..));

    private interface I {}
    declare parents: C implements I;

    before(I i): make(i) {    //ERR: doesn't match Object
        System.out.println("new I: " + i);
    }
}
