import org.aspectj.testing.*;
public class CannotReferenceSuper {

    public static void main(String[] args) {
        new CannotReferenceSuper().go();
    }

    static {
        Tester.expectEventsInString("a.go,aa.go,b.go,bb.go");
    }

    void go() {
        new A().go();
        new B().go();
        Tester.checkAllEvents();
    }
    
    class A {
        class AA extends A {
            void go() { Tester.event("aa.go"); }
        }
        void go() { Tester.event("a.go"); new AA().go(); }
    }

    class B extends A {        
        class BB extends AA {
            void go() { Tester.event("bb.go"); }
        }
        void go() { Tester.event("b.go"); new BB().go(); }
        
    }
}


