public class Parents {
	public static void main(String[] args) {
		A.I i = new C1();
		i.m();
		
		C2 c2 = new C2();
		c2.m1();
		
		A.C3 c3 = (A.C3)c2;
		
		C4 c4 = new C4();
		c4.m4();
	}
}

class C1 {
	public void m() { System.out.println("m"); }
}

class C2 {}

class C4 {}

aspect A {
	static class C3 {
		public void m1() { System.out.println("from C3"); }
	}
	
	interface I {
		void m();
	}

	declare parents: (C1 && !C2) implements I;
	declare parents: C2 extends C3;
	
	interface I4 {}
	public void I4.m4() { System.out.println("I.m4"); }
	
	declare parents: C4 implements I4;
}