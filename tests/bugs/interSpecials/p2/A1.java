package p2;

import p1.C;

public class A1 {
	public static void main(String[] args) {
		new C().foo();
	}
}

aspect InterClass {
	void C.foo() {
		System.out.println("class: " + C.class);
	}
}