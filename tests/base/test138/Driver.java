//only register things once. 

import org.aspectj.testing.Tester;

public class Driver {
    public static void test() {
        A a = new A();
        B b = new B();

	// instances of A, B are created (the instanceof Aspect isn't counted for timing reasons)
        Tester.checkEqual(Aspect.count, 2, "instance count");
    }

    public static void main(String[] args) { test(); }
}


class A {
    public A() {}
    public void foo() {}
}

class B extends A {
    public B() {}
    public void bar() {}
}

aspect Aspect {
    public static int count = 0;
    
    after() returning(): /*target(*) &&*/ call(new()) {
        count++;
    }
}
