import org.aspectj.testing.Tester;

public class Simple {
    public static void main(String[] args) {
	new C().foo();
    }
}

class C {
    private int privateField = 1;

    private int privateMethod() {
	return 42;
    }

    public void foo() {
	System.out.println("f: " + privateField);
    }
}

privileged aspect A {
    static before (C c): instanceof(c) && receptions(void foo()) {
	System.out.println("from A: " + c.privateField);
	c.privateField += 1;
	System.out.println("from A: " + c.privateField);
	System.out.println("from A: " + ++c.privateField);
	System.out.println("from A: " + c.privateField++);
	System.out.println("from A: " + c.privateField);
	System.out.println("from A: " + c.privateMethod());
    }
}
