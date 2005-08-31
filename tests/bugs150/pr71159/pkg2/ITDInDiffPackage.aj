package pkg2;
import pkg1.*;

public aspect ITDInDiffPackage {
	
	void B.foo() {}
	
	void bar() {
		C c = new C();
		c.foo();
	}
	
	declare warning : call(* B.foo()) : "should not match";
	declare warning : call(* C.foo()) : "should match";
	
	
}