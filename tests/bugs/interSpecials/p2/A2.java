package p2;

import p1.C;
public class A2 {
	public static void main(String[] args) {
		new C().foo();
	}
}

aspect InterClass {
	void C.foo() {
		assert(C.class != null);
	}
}
