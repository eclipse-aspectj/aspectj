class A {}
interface I {}

public class InterfaceArrayCast {

    public static void main(String[] args) {}

    void foo(A[] as, I i) {
	A[] as1 = (A[])i;
	I i1 = (I)as;
    }
}

