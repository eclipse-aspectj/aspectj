public class Fails {

	interface I {
		static final int CONST = 56;
	}

	static class A<T> {
		protected int prot;
		protected String protS;
		int def;
		String defS;
		T foo;
	}

	static class B extends A<String> implements I {
		void m() {
			// protected
//			super.prot = 1;
//			super.protS = "1";
//			System.out.println(super.protS + super.prot);
			prot = 2;
			protS = "2";
			System.out.println(protS + prot);
			// default
			super.def = 1;
			super.defS = "1";
			System.out.println(defS + def);
			def = 2;
			defS = "2";
			foo = "todo";
			System.out.println(defS + def);
//			// interface
//			System.out.println(CONST);
		}
	}

	public static void main(String[] args) {
		B b = new B();
		b.m();
	}
}

