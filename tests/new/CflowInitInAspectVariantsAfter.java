import org.aspectj.testing.Tester; 

public class CflowInitInAspectVariantsAfter {
	public static void main(String[] args) { 
		new C().a();
		Tester.checkAllEventsIgnoreDups();
	}
	static {
		Tester.expectEvent("cflow after pc()");
		Tester.expectEvent("cflow after execution(void C.a())");
		Tester.expectEvent("cflowbelow after pc()");
		Tester.expectEvent("cflowbelow after execution(void C.a())");
	}
}
class C {
	void a() {b();}
	private void b() {int i = 1;} // avoid later optimizations
}

aspect A {
	
	pointcut safety() : !within(A);
	pointcut pc() : execution(void C.a());
	after() : safety() && cflow(pc()) { 
		Tester.event("cflow after pc()");
	}
	after() : safety() && cflow(execution(void C.a())) { 
		Tester.event("cflow after execution(void C.a())");
	}
	after() : safety() && cflowbelow(pc()) { 
		Tester.event("cflowbelow after pc()");
	}
	after() : safety() && cflowbelow(execution(void C.a())) { 
		Tester.event("cflowbelow after execution(void C.a())");
	}
}
