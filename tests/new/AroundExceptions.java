import org.aspectj.testing.Tester;
import java.io.IOException;

public class AroundExceptions {
    public static void main(String[] args) {
	new SubC().m();
	Tester.checkAndClear("around-reception caught-IOException", "subc.m");

	new C().m();
	Tester.checkAndClear("around-reception", "c.m");
    }
}

class C {
    public void m() throws IOException {
    }
}

class SubC extends C {
    public void m() throws IOException {
	throw new IOException();
    }
}
    


aspect A {
    void around (): call(void C.m()) {
	Tester.note("around-reception");
	proceed();
    }

    void around(): call(void C+.m()) {
	try {
	    proceed();
	} catch (IOException ioe) {
	    Tester.note("caught-IOException");
	}
    }
}
