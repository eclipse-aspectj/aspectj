public class AroundNames {
	public static void main(String[] args) {
		new Base().doit();
		new Derived().doit();
	}
}

class Base {
	static private final void m() {}
	
	public void doit() {
		m();
	}
}

class Derived {
	static private final void m() { return; } // "Derived"; }
	
	public void doit() {
		m();
	}
}

aspect A {
	Object around(): execution(* m()) {
		return proceed();
	}
}