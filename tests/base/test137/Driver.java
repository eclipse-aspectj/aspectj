// operations on private && protected aspect members (++, -- in partciular)

import org.aspectj.testing.Tester;

public class Driver {
    public static void main(String[] args) { test(); }

    public static void test() {
        Foo foo = new Foo();

        foo.foo();

        Tester.checkEqual(Aspect.getPrivateAspectVar(), 23);
        Tester.checkEqual(Aspect.aspectOf().getPrivateAspectInstanceVar(), 23);
    }
}

class Foo {
    public void foo() {
    }
}

//XXX need to put some of eachobject back into this some day

aspect Aspect { 
    private static final int PRIVATEASPECTCONST            = 10;
    private static       int privateAspectVar              = 20;

    protected static     int protectedAspectVar            = 40;

    public static int getPrivateAspectVar() {
        return privateAspectVar;
    }

    private              int privateAspectInstanceVar      = 30;
    protected            int protectedAspectInstanceVar    = 50;

    public int getPrivateAspectInstanceVar() {
        return privateAspectInstanceVar;
    }



    pointcut onFoo(): target(Foo) && call(void foo());

     before(): onFoo() {
        privateAspectVar = 21;
        privateAspectVar = 1 + privateAspectVar;
        Tester.checkEqual(privateAspectVar, 22);
        Tester.checkEqual(privateAspectVar += 1, 23);
        Tester.checkEqual(privateAspectVar++, 23);
        Tester.checkEqual(privateAspectVar, 24);
        Tester.checkEqual(privateAspectVar--, 24);
        Tester.checkEqual(privateAspectVar, 23);
        Tester.checkEqual(++privateAspectVar, 24);
        Tester.checkEqual(privateAspectVar, 24);
        Tester.checkEqual(--privateAspectVar, 23);
    }    

    before(): onFoo() {
        privateAspectVar = 21;
        privateAspectVar = 1 + privateAspectVar;
        Tester.checkEqual(privateAspectVar, 22);
        Tester.checkEqual(privateAspectVar += 1, 23);
        Tester.checkEqual(privateAspectVar++, 23);
        Tester.checkEqual(privateAspectVar, 24);
        Tester.checkEqual(privateAspectVar--, 24);
        Tester.checkEqual(privateAspectVar, 23);
        Tester.checkEqual(++privateAspectVar, 24);
        Tester.checkEqual(privateAspectVar, 24);
        Tester.checkEqual(--privateAspectVar, 23);
        Tester.checkEqual(privateAspectVar, 23);

        privateAspectInstanceVar = 21;            
        privateAspectInstanceVar = 1 + privateAspectInstanceVar;
        Tester.checkEqual(privateAspectInstanceVar, 22);
        Tester.checkEqual(privateAspectInstanceVar += 1, 23);
        Tester.checkEqual(privateAspectInstanceVar++, 23);
        Tester.checkEqual(privateAspectInstanceVar, 24);
        Tester.checkEqual(privateAspectInstanceVar--, 24);
        Tester.checkEqual(privateAspectInstanceVar, 23);
        Tester.checkEqual(++privateAspectInstanceVar, 24);
        Tester.checkEqual(privateAspectInstanceVar, 24);
        Tester.checkEqual(--privateAspectInstanceVar, 23);
        Tester.checkEqual(privateAspectInstanceVar, 23);
    }
}
