import org.aspectj.testing.Tester;

public class AfterConstructorCalls {
    public static void main(String[] args) {
        new Foo().bar();
    }
}

class Foo {
    int bar() { return 0; }
}

aspect A {
    pointcut nimboCut() : 
        call(Foo.new(..));
    
    /*static*/ after() returning (Object o): nimboCut() {
        Tester.check(o != null && o instanceof Foo, o + " !instanceof Foo");
    }

    /*static*/ after() returning (int i): call(int Foo.*(..)) {
        Tester.checkEqual(i, 0);
    }
}
