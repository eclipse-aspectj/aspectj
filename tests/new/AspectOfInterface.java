import org.aspectj.testing.Tester;

public class AspectOfInterface {
    public static void main(String[] args) { test(); }

    public static boolean ranAdvice = false;
    public static void test() {
	new C().foo();
	Tester.check(ranAdvice, "advice on interface");
    }
}

interface I {
    public void foo();
}

class C implements I {
    public void foo() { } //System.out.println("foo"); }
}

aspect A /*of eachobject(instanceof(I))*/ {
    before(): call(* *(..)) {
	AspectOfInterface.ranAdvice = true; //System.out.println("before");
    }
}
