import org.aspectj.testing.*;

public class BindingThisInsteadOfFormal {
    public static void main(String[] args) {
        Caller c = new Caller();
        c.goo();
        Tester.checkAllEvents();
    }

    static {
        Tester.expectEvent("before-string");
        Tester.expectEvent("before-go");
        Tester.expectEvent("before-static");
        Tester.expectEvent("before-c");
    }
}

class Caller {
    void goo() {
        go();
	staticGo();
    }
    void go() {
        String string = new String("string");
        C c = new C();
    }

    static void staticGo() {
    }
}

class C {

}

aspect Aspect perthis(this(Caller)) {
    pointcut stringCtors(): call(String.new(String));
    before(): stringCtors() {
        Tester.event("before-string");
    }

    pointcut cCtors(): call(C.new());
    before(): cCtors() {
        Tester.event("before-c");
    }    

    pointcut goCalls(Caller caller): call(void go()) && target(caller);
    before(Caller caller): goCalls(caller) {
        Tester.event("before-go");
	Tester.check(caller != null, "instance method");
    }        

    pointcut goStaticCalls(): call(void Caller.staticGo());
    before(): goStaticCalls() {
        Tester.event("before-static");
    }        
}
