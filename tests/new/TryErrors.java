import org.aspectj.testing.Tester;

public class TryErrors {
    public static void main(String[] args) {
        test();
    }

    public static void test() {
        Foo foo = new Foo();
        foo.bar();
        Tester.check(foo.memberAdvised, "member advice happened");
        Tester.check(foo.signatureAdvised, "signature advice happened");
    }
}

class Foo {
    boolean memberAdvised = false;
    boolean signatureAdvised = false;

    public void bar() {
	    ;
    }
}

aspect A {
    /*static*/ after(Foo foo): target(foo) && within(Foo) && execution(void bar()) {
	foo.memberAdvised = true;
     }

    /*static*/ before(Foo foo): target(foo) && call(void bar()) {
	foo.signatureAdvised = true;
    }
}
