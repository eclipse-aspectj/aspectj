import org.aspectj.testing.Tester; 

//PR#493
public class NonAlphaSignaturePatternCE {
	public static void main(String[] args) {
		C1 c = new C1();
		c.m1();
		c.update();
		Tester.checkAllEventsIgnoreDups();
	}
}

class C1 {
	public int fi1;
    public float f1 = 1.f;
	void cflowbelow() {  } // cflowbelow join points; also test keyword collision
	void m1() { String s = ("m1 "); cflowbelow(); }
	void update() { 
		fi1 = 1;       // set join point
		int i = fi1;   // get join point
	}
}
// PR#493 Signature patterns without leading alphabetic characters
aspect A {
	static {
		// expect all of these messages when test runs
		Tester.expectEvent("call m1");
		Tester.expectEvent("before call *1");
		Tester.expectEvent("before p1");
		Tester.expectEvent("before execution *1");
		Tester.expectEvent("initialization *1");
		Tester.expectEvent("staticinitialization *1");
		Tester.expectEvent("withincode *1");
		Tester.expectEvent("cflow *1");
		Tester.expectEvent("cflowbelow *1");
		Tester.expectEvent("set *1");
		Tester.expectEvent("get *1");
	}
	after () : call(void m1()) { Tester.event("call m1"); } // normal case

	// @testTarget signature.patterns.leadingnumeric.pointcut 
	pointcut p1(): call(void *1()) ; // incorrect compiler error here PR#493

	before () : p1(){ // incorrect compiler error here PR#493
		Tester.event("before p1");
	}
	// @testTarget signature.patterns.leadingnumeric.anonpointcut.call 
	before () : call(void *1()) { // incorrect compiler error here PR#493
		Tester.event("before call *1");
	}

	// @testTarget signature.patterns.leadingnumeric.anonpointcut.execution 
	after () : execution(void *1()) {// incorrect compiler error here PR#493
		Tester.event("before execution *1");
	}

	// @testTarget signature.patterns.leadingnumeric.anonpointcut.initialization 
	after () : initialization(*1.new()) {// incorrect compiler error here PR#493
		Tester.event("initialization *1");
	}

	// @testTarget signature.patterns.leadingnumeric.anonpointcut.staticinitialization 
	before () : staticinitialization(*1) {// incorrect compiler error here PR#493
		Tester.event("staticinitialization *1");
	}

	// @testTarget signature.patterns.leadingnumeric.anonpointcut.withincode 
	before () : withincode(void C1.*1()) {// incorrect compiler error here PR#493
		Tester.event("withincode *1");
	}

	// @testTarget signature.patterns.leadingnumeric.anonpointcut.set 
	before () : set(int C*1.fi1) {// incorrect compiler error here PR#493
		Tester.event("set *1");
	}
 
	// @testTarget signature.patterns.leadingnumeric.anonpointcut.get 
	before () : get(int *.*1) {// incorrect compiler error here PR#493
		Tester.event("get *1");
	}
 
	// @testTarget signature.patterns.leadingnumeric.anonpointcut.set 
}

/**
 * Moved advice on cflow to B in order to continue testing this
 * as well as possible without forcing a StackOverflowError
 */
aspect B {
	// @testTarget signature.patterns.leadingnumeric.anonpointcut.cflowbelow
	before () : cflowbelow(execution(void *1.m1())) && !within(B) {
		Tester.event("cflowbelow *1");
	}
	// @testTarget signature.patterns.leadingnumeric.anonpointcut.cflow 
	before () : cflow(execution(void *1.m1())) && !within(B) {
		Tester.event("cflow *1");
	}
}
