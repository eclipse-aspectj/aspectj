import org.aspectj.testing.Tester;

import java.io.IOException;

public class ExceptionsCP {
    public static void main(String[] args) {
        C c = new C();
        try {
        	c.foo();
        } catch (IOException io) {}
        c.bar();
    }
}
class Root {
	public void bar() {}
}

class C extends Root {
	
}


aspect A {
	public void C.foo() throws IOException { }
}
