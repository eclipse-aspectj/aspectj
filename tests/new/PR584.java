
import org.aspectj.testing.Tester;
import org.aspectj.testing.Tester; 

public class PR584 {

    public static void main(String[] args) {
        Tester.expectEvent("foo ok");
        Tester.expectEvent("foo test2");
        Foo foo = new Foo("foo");
        /** @testcase PR#584 constructing inner classes using qualified new expression */
        Foo.Test test1 = foo.new Test();
        Foo.Test test2 = foo.new Test() { 
                public void foo() {
                    Tester.event(getFoo().baz + " test2"); 
                } 
            };
        test1.foo();
        test2.foo();
        /** @testcase PR#584 constructing static inner classes using new expression */
        Foo.StaticTest test3 = new Foo.StaticTest();
        Tester.expectEvent("static foo");
        test3.staticBaz = "static foo";
        test3.staticFoo();
        Tester.checkAllEvents();
    }
}

class Foo {
    public String baz;
    public Foo(String baz) { this.baz = baz; }
    public static class StaticTest {
        static String staticBaz;
        public void staticFoo() { Tester.event(staticBaz); }
    }
    public class Test {
        public Foo getFoo() { return Foo.this; }
        public void foo() { Tester.event(baz + " ok"); }
    }
}
