import org.aspectj.testing.Tester;

public class MultiDispatchCp {
    public static void main(String[] args) {
        C c = new C();

        Tester.event("**** exec ****");
        MultiExec.enabled = true;
        run(new C());

        Tester.event("**** call ****");
        MultiExec.enabled = false;
        MultiCall.enabled = true;
        run(new C());

        Tester.event("**** both=call ****");
        MultiExec.enabled = true;
        run(new C());

        Tester.checkEventsFromFile("MultiDispatchCp.out");
        //Tester.printEvents();
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

abstract aspect Multi {
    abstract pointcut m();

    abstract String getPrefix();

    String around(String s): m() && args(s) {
        return getPrefix() + "-string-" + s;
    }
    String around(Integer i): m() && args(i) {
        //System.out.println(thisJoinPoint + " would return " + proceed(i));
        return getPrefix() + "-integer-" + i;
    }
    String around(Double d): m() && args(d) {
        return getPrefix()  + "-double-" + d;
    }
}

aspect MultiCall extends Multi {  
    public static boolean enabled = false;

    String getPrefix() { return "call"; }

    pointcut m(): call(String C.doit(Object)) && if(enabled);
}


// dominates should have no effect as call join points
// always come before executions
aspect MultiExec extends Multi { declare precedence: MultiExec, MultiCall;
    public static boolean enabled = false;

    String getPrefix() { return "exec"; }

    pointcut m(): execution(String C.doit(Object)) && if(enabled);
}
