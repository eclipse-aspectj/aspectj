import org.aspectj.testing.Tester;

public class TypeDeclInAdvice {
  public static void main(String[] args) { test(); }

  public static void test() {
    Foo foo = new Foo();
    foo.m();
  }
}

aspect A {
    void around(): this(Foo) && call(void m(..)) {
	class Internal {
	    int val() { return 1; }
	}
	int i = 0;
	Internal j;
	
	i = 1;
	j = new Internal();
	
	proceed();

	//System.out.println("j: " + j);
	
	Tester.checkEqual(i, 1, "i");
	Tester.checkEqual(j.val(), 1, "j.val()");
    }
}

class Foo {
    Foo() {
	//System.out.println("constructor Foo()");  
    }
    void m() {
	//System.out.println("method m()");
    }
}
