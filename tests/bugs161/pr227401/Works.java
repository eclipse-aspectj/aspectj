public class Works {

	interface I {
		static final int CONST = 56;
	}

	static class A {
		protected int prot;
		protected String protS;
		int def;
		String defS;
		String foo;
	}

	static class B extends A implements I {
		void m() {
//			// protected
//			super.prot = 1;
//			super.protS = "1";
//			System.out.println(super.protS + super.prot);
			prot = 2;
			protS = "2";
			System.out.println(protS + prot);
//			// default
//			super.def = 3;
//			super.defS = "3";
//			System.out.println(defS + def);
//			def = 4;
//			defS = "4";
//			foo = "todo";
//			System.out.println(defS + def);
//			// interface
//			System.out.println(CONST);
		}
	}

	public static void main(String[] args) {
		B b = new B();
		b.m();
	}
}
