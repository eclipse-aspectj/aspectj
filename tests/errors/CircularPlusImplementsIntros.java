public class CircularPlusImplementsIntros {
    public static void main(String[] args) {
        org.aspectj.testing.Tester.check(false, "shouldn't have compiled!");
    }
}

interface I {}
interface J {}

class C {
    {
        I i = new I() {};
        J j = new J() {};
    }
}

aspect A {
    declare parents: I implements J;
    declare parents: J implements I;
}
