package org.aspectj.profiling;

public aspect WorkTheWeaver {
	
	before() : execution(* set*(..)) {
		System.out.println("before setter...");
	}
	
	after() returning : execution(* set*(..)) {
		System.out.println("after returning from setter...");
	}
	
	// using "call" on an interface type will cause us to chase
	// all the way up the inheritance hierarchies of any type we
	// call.
	// it also means we have to crack open methods when weaving...
	declare warning : call(* java.lang.Runnable+.*(..)) 
	  : "call to a subtype of runnable";
	
	
}