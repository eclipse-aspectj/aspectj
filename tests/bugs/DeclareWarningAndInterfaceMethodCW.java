
/*
 * ajc fails to detect call join points using the
 * declaring interface as the type when the method
 * was declared in the aspect.
 */

public class DeclareWarningAndInterfaceMethodCW {
    public static void main(String[] args) {
        new C().doSomething();
    }
}

interface ICanGetSomething {
    Object getSomething();
}

class B implements ICanGetSomething {
    public Object getSomething() { // CE conflict here?
        return new Object();
    }
}

class C {

    C() {
        new B().getSomething(); // 2 CW declared here
    }

    static void staticMethod() {
        new B().getSomething(); // 2 CW declared here
        B b = new B();
        b.getSomething(); // 2 CW declared here
        ICanGetSomething i = new B();
        i.getSomething(); // 2 CW declared here
    }
    void doSomething() {
        Object result = new B().getSomething(); // 2 CW here
        if (null == result) {
            throw new Error("no result");
        }
    }
}

/** @testcase PR#40805 interface call signatures when declaring method in aspect */
aspect A { 
    // comment this out to get correct warnings
    public Object ICanGetSomething.getSomething() { 
        return null; 
    }
    declare warning : call(Object ICanGetSomething.getSomething())
        : "call ICanGetSomething.getSomething()";
    declare warning : call(Object getSomething())
        : "call getSomething()";
}
