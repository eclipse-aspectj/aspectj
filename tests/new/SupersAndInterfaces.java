import org.aspectj.testing.Tester;

import java.io.IOException;

public class SupersAndInterfaces {
    public static void main(String[] args) throws IOException {
	new C2().m();
	Tester.check("ran before");
	new C().toString();
	Tester.check("ran before toString");
    }
}

class C {
    public void m() throws IOException { 
	if (false) throw new IOException("testing");
    }
    public String toString() {
	return super.toString() + "C";
    }
}

interface I {
    public void m() throws IOException;
}

class C1 extends C implements I {
}

class C2 extends C1 {
    static boolean ranBody;
    public void m() throws IOException {
	ranBody = true;
	super.m();
    }
}

aspect A {
    before(): call(void m()) {
	Tester.note("ran before");
	Tester.check(!C2.ranBody, "first entry");
    }
    before(): call(String toString()) {
	Tester.note("ran before toString");
    }
}
