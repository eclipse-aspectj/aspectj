import org.aspectj.testing.Tester;
import java.io.*;
import org.aspectj.lang.*;

public class DeclareSoft {
    public static void main(String[] args) {
        new C().m1();
        try {
            new C().m2();
        } catch (SoftException se) {
            Tester.note("m2-soft");
        }

        try {
            new C().m3();
        } catch (SoftException se) {
            Tester.check(false, "already caught");
        }

        try {
            new C().throwIt();
        } catch (SoftException se) {
            Tester.note("throwIt-soft");
        } catch (Throwable t) {
            Tester.check(false, "should have been softened: " + t);
        }

        try {
            new C().pretendsToThrow();
        } catch (IOException ioe) {
            Tester.check(false, "bad IO");
        }

        Tester.check("m2-soft");
        Tester.check("around-m3");
    }
}

class C {
    public void throwIt() throws Throwable {
        throw makeThrowable();
    }

    public void pretendsToThrow() throws IOException, ClassNotFoundException {

    }

    private Throwable makeThrowable() {
        return new Exception("make me soft");
    }


    public void m1() {
    }

    public void m2() {
        new File("___hi").getCanonicalPath();
        new FileInputStream("___bye");
    }

    public void m3() {
        new FileInputStream("___bye");
        new File("___hi").getCanonicalPath();
    }
}

aspect B {
    declare soft: Exception: execution(* C.throwIt());

    declare soft: ClassNotFoundException: call(* C.pretendsToThrow());
}


aspect A {
    declare soft: IOException: execution(* C.*(..));

    void around(): execution(void C.m3()) {
        try {
            proceed();
        } catch (IOException ioe) {
            Tester.note("around-m3");
        }
    }
}
