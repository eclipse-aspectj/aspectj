package p2;

import p1.C;
public class A2 {
	public static void main(String[] args) {
		new C().foo();
		new C().bar();
	}
}

aspect InterClass {
	void C.foo() {
		assert(C.class != null);
	}
	
	void around(): execution(void C.bar()) {
		assert(C.class != null);
		proceed();
	}
}
