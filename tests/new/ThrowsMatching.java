import org.aspectj.testing.Tester;

public class ThrowsMatching {
    public static void main(String[] args) throws Exception {
        C c = new C();
        c.m1();
        c.m2();

        Tester.checkEqual(A.buf.toString(), "before:m1:m2:");
    }
}


class E1 extends Exception { }

class C {
    public void m1() throws E1 { A.buf.append("m1:"); }
    public void m2() { A.buf.append("m2:"); }
}

aspect A {
    static StringBuffer buf = new StringBuffer();

    before(): call(void C.m*() throws E1) { A.buf.append("before:"); }
}
