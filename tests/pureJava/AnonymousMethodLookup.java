public class AnonymousMethodLookup {
    public static void main(String[] args) {
	new C() { { foo(); } };
    }
}

class C {
    void foo() {}
}
