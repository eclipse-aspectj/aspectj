
import org.aspectj.testing.Tester;

/** @testcase PR#900 after advice on static call jp */
public class AfterStaticCall {
    public static void main(String[] args) {
		Tester.expectEvent("foo()");
		Tester.expectEvent("after() : call(void Test.foo())");
		foo();
		Tester.checkAllEvents();
    }

    public static void foo() {
    	Tester.event("foo()");
    }
}

aspect LogFooCall {
    after() : call(static void foo()) {
		Tester.event("after() : call(void Test.foo())");
	}
}