public class QualifiedThisMatchesExactly {
    public static void main(String[] args) {
	System.err.println("shouldn't compile!!!");
    }
}

class A {
    class B {}
}

class C extends A {
    static class D extends B {
	D() {
	    super();
	}
	void foo() {
	    System.err.println(D.this);
	}
    }
}
