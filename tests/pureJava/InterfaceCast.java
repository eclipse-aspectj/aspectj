class A {}
interface I {}
class B extends A implements I {}

public class InterfaceCast {

    public static void main(String[] args) {}

    void foo(A a, B b, I i) {
	A a0 = a;
	A a1 = b;
	A a2 = (A)i;

	B b0 = (B)a;
	B b1 = b;
	B b2 = (B)i;

	I i0 = (I)a;
	I i1 = b;
	I i2 = i;
    }
}

