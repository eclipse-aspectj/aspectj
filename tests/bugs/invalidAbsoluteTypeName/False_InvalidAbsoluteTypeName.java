

/** @testcase PR#65925 Valid but inaccessible type names should not be flagged by XLint:invalidAbsoluteTypeName */
public class False_InvalidAbsoluteTypeName {
    public static void main(String[] args) {
		C.go();
	}
}

class C {
    static void go() {
        Nested.method();
    }

    // remove "private" to work around bug
    private static class Nested {
        static void method() {}    // CW 17 per declare
    }
}

aspect A {
    // bug: trigger XLint:invalidAbsoluteTypeName b/c not visible, though valid
    declare warning : execution(void C.Nested.method()) : "nested method";
}