import org.aspectj.testing.Tester;

/** @testcase replacing this or target in around advice */
public class TargetObjectReplacement {
    public static void main(String[] args) {
        Foo f = new Foo("orig");
        // replacing target or this in advice does not affect caller reference
        f.m();
        f.n();
        f.o();
        f.p();
        Tester.checkEqual(f.idhat, "orig"); // no affect presumed
        Tester.checkAllEvents();
    }
    static {
        Tester.expectEvent("m: m-delegate");
        Tester.expectEvent("n: n-delegate");
        Tester.expectEvent("o: o-delegate");
        Tester.expectEvent("p: orig");      // no affect presumed
    }
}

class Foo {
    String id;
    String idhat;

    Foo(String id) { 
        this.id = id; 
        this.idhat = id;
    }

    void m() {
        Tester.event("m: " + id);
    }

    void n() {
        Tester.event("n: " + id);
    }

    void o() {
        Tester.event("o: " + id);
    }

    void p() {
        Tester.event("p: " + id); // before around advice on idhat get
        Tester.checkEqual(idhat, "p-delegate"); // callees affected
    }
}

aspect A {

    void around(Foo foo): target(foo) && call(void m())      {
        proceed(new Foo("m-delegate"));
    }
    void around(Foo foo): target(foo) && execution(void n()) {
        proceed(new Foo("n-delegate"));
    } 
    void around(Foo foo): this(foo) && execution(void o())   {
        proceed(new Foo("o-delegate"));
    }
    String around(Foo foo): target(foo) && get(String Foo.idhat) && within(Foo) {
        return proceed(new Foo("p-delegate"));
    }
}
