import org.aspectj.testing.Tester;

public class MethodLocalAroundReturns {
    public static void main (String[] args) {
        C c = C.make();
        Tester.check(null != c, "null c");
        Tester.check("ok".equals(c.toString()), "bad c: " + c);
    } 
}

class C {
    static C make() { return null; }
}

aspect A {
    /** @testcase method-local class defined in around return statement */
    C around() : call(C C.make()) {
        return new C() {
                public String toString() { return "ok"; } // bad compiler error here
            };
    }
}
