package x;

abstract aspect Aspect {
	
	abstract pointcut scope();
	
	before(): call(* foo(..)) && cflow(execution(* toplevel(..))) && scope() {
		System.out.println("advice fired");
	}
}