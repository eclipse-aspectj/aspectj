import org.aspectj.testing.Tester;

import java.io.IOException;

public class ExceptionsCF {
    public static void main(String[] args) {
        C c = new C();
        c.foo();  // ERR: can't throw IOException here
    }
}
class Root {
	public void bar() {}
}

class C extends Root {
	
}


aspect A {
	public void C.foo() throws IOException { }
	
	public void C.bar() throws IOException {}  // ERR: can't throw more than super
}
