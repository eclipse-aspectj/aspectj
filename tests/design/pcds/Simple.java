package pcds;

import org.aspectj.testing.Tester;

public class Simple {
    public static void main(String[] args) {
        C c = new C();
        c.m("hi");

        C subc = new SubC();
        subc.m("hi");
        subc.m(new Integer(1));

        SubC subc1 = new SubC();
        subc1.m("bye");

        subc.hashCode();
    }
}

class C {
    void m(Object o) {
        System.out.println("C.m(" + o + ")");
    }

    static pointcut meths(C c): call(void m(Object)) && target(c);
}

class SubC extends C {
    SubC(int x) {
        System.out.println("x: " + x);
    }

    SubC(String s) {
        this(2*2);
        System.out.println("s: " + s);
        int x = 10;
    }

    SubC() {
        this("hi");
        System.out.println("no args");
    }
}


aspect A {
    before(Object o): C.meths(o) {
        System.out.println("static named pointcut");
    }

    before(): call(void m(..)) && target(SubC) && args(String) {
        System.out.println("dmatches: " + thisJoinPoint);
    }
    before(): call(void SubC.m(String)) {
        System.out.println("!smatches: " + thisJoinPoint);
    }
    before(Object o, String s): call(void C.m(Object)) && target(SubC) && args(s) && args(o) {
        System.out.println("smatches: " + thisJoinPoint +", " + s +", " + o);
    }

    before(): initialization(SubC.new(..)) {
        System.out.println(thisJoinPoint + "new SubC");
    }
    void around(): initialization(SubC.new(..)) {
        proceed();
    }

    before(): execution(SubC.new(..)) {
        System.out.println(thisJoinPoint + "new SubC");
    }

    before(): call(int Object.hashCode()) {
        System.out.println("hashCode()");
    }
    before(): call(int Object.hashCode()) && target(C) {
        System.out.println("hashCode() on C");
    }
}
    
