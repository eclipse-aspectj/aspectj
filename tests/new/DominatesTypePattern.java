import org.aspectj.testing.Tester;

/** @testcase subtype pattern in dominates should pick out aspect subtypes */
public class DominatesTypePattern {
    public static void main (String[] args) {
        String s = new C().method();
        Tester.check("pass".equals(s),
                     "\"pass\".equals(\"" + s + "\")");
    } 
}

class C {}

// works if A is specified explicitly
abstract aspect AA { declare precedence: AA, (AA+ && !AA); // error: should dominate A
    public String C.method() { return "pass"; }
}

aspect A extends AA { 
    public String C.method() { return "fail"; }
}
