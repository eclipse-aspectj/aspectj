import org.aspectj.testing.Tester;

class Sup {
    static void m() {}
}

public class SuperStaticCallJoinPoint extends Sup {
    static boolean ran = false;
    public static void main(String[] args) {
	new SuperStaticCallJoinPoint().foo();
	Tester.check(ran, "didn't run advice");
    }
    void foo() {
	super.m();
    }
    static void m() {
	throw new RuntimeException();
    }
}

aspect A {
    before(): this(SuperStaticCallJoinPoint) && call(void Sup.m()) {
	SuperStaticCallJoinPoint.ran = true;
    }
}
