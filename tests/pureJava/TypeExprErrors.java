// errors on lines:
// 10 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 36 37

public class TypeExprErrors {
    static String s;
    static boolean b;
    static A a;

    TypeExprErrors() {
        this(A);
    }

    TypeExprErrors(Object o) {
    }

    static {
        s += A;
        a = A;
        f(A);
        f((A) A);
        f(b ? A : a);
        f(b ? a : A);
        new TypeExprErrors(A);
        ff(a == A);
        ff(A == a);
        ff(A != a);
        ff(a != A);
        ff(A != null);
        ff(null != A);
        ff(A == null);
        ff(null == A);
        ff(A instanceof A);
        f(new A[] { A });
        (A).m();
        (A).sm();               // not actually an error
        f(s + A);
        f(A + s);
    }

    static void f(Object o) {
    }
    static void ff(boolean b) {
    }
}

class A {
    void m() {}
    static void sm() {}
}

