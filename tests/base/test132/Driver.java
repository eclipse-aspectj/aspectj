//  proper matching of overloaded constructors

import org.aspectj.testing.Tester;

public aspect Driver {
    public static void main(String[] args) { test(); }
    after(/*Foo f,*/ String aspectName) returning(Foo f): 
        /*target(f) &&*/ call(new(String)) && args(aspectName) {
        f.name = aspectName+"-ADVISED";
    }

    public static void test() {
        Foo foo = new Foo("NAME");
        Tester.checkEqual(foo.name, "NAME-ADVISED", "new Foo(name)");
        foo = new Foo();
        Tester.checkEqual(foo.name, "NONE", "new Foo()");
    }
}

class Foo {
    public String name = "NONE";
    public Foo() { }
    public Foo(String name) {
        this.name = name;
    }
}
