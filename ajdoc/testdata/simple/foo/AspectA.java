
package foo;

public aspect AspectA {
	
	int foo;
	
	pointcut mumblePointcut(): execution(* ClassA.*(..));
	
	before(): mumblePointcut() {
		System.err.println("yo");
	}

	after(): mumblePointcut() {
		System.err.println("yo");
	}
} 