import org.aspectj.testing.Tester;

public class Driver {
    public static void main(String[] args) { test(); }
    
    public static void test() {
	Foo foo = new Foo();
	foo.m();
    }
}

aspect A {
    //static advice(): Foo && (void m(..) || new(..)) {
    //!!!no around advice allowed on constructors right now
    void around(): target(Foo) && call(void m(..)) {
	class Internal {
	    int val() { return 1; }
	}
	int i = 1;
	Internal j = new Internal();
	proceed();
	Tester.checkEqual(i, 1, "i");
	Tester.checkEqual(j.val(), 1, "j.val()");
    }
}

class Foo {
    Foo() {
	// System.out.println("constructor Foo()");  
    }
    void m() {
	// System.out.println("method m()");
    }
}
