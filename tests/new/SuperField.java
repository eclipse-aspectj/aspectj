import org.aspectj.testing.Tester;

public class SuperField {
    public static void main(String[] args) {
        Derived d = new Derived();
        Tester.checkAndClearEvents(new String[] {
            "set Base on <Derived>",
            "set Derived on <Derived>",
                });
        d.m1();
        Tester.checkAndClearEvents(new String[] {
            "get Derived on <Derived>",
            "Derived",
                });
        d.m2();
        Tester.checkAndClearEvents(new String[] {
            "get Derived on <Derived>",
            "Derived",
                });
        d.m3();
        Tester.checkAndClearEvents(new String[] {
            "get Base on <Derived>",
            "Base",
                });
        d.m4();
        Tester.checkAndClearEvents(new String[] {
            "get Base on <Derived>",
            "set BaseNew on <Derived>",
            "get BaseNew on <Derived>",
            "BaseNew",
                });
        Tester.printEvents();
    }
}

class Base {
    String s = "Base";
    public String toString() { return "<Base>"; }
}

class Derived extends Base {
    String s = "Derived";


    void m1() {
        Tester.event(s);
    }

    void m2() {
        Tester.event(this.s);
    }

    void m3() {
        Tester.event(super.s);
    }

    void m4() {
        super.s += "New";
        
        Tester.event(super.s);
    }

    public String toString() { return "<Derived>"; }
}

aspect A {
    after(Object o) returning (String v): target(o) && get(String s) {
        Tester.event("get " + v + " on " + o);
    }

    before(Object o, String v): target(o) && set(String s) && args(v) {
        Tester.event("set " + v + " on " + o);
    }


}
