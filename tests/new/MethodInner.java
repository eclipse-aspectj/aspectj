import org.aspectj.testing.Tester;

public class MethodInner {
    public static void main(String[] args) { test(); }

    public static void test() {
	Tester.checkEqual(new C().foo(), 10, "inner");
    }
}

class C {
    int foo() {
	final int N = 10;

	class Inner {
	    public int bar() {
		return N;
	    }
	}

	return new Inner().bar();
    }
}

aspect A issingleton() {
    before(): execution(int *(..)) {
	System.out.println("before: " + thisJoinPoint);
    }
}
