abstract aspect SuperAspect<T> {
	
	before() : execution(* *(T)) {
		System.out.println("I matched at " + thisJoinPointStaticPart);
	}
	
}

public aspect AnonymousPointcutInGenericAspect extends SuperAspect<String> {
	
	public static void main(String[] args) {
		C c = new C();
		c.foo("well i never");
		c.bar(5);
	}
	
}

class C {
	
	// should be matched
	public void foo(String s) {}
	
	// should not be matched
	public void bar(Number n)  {}
	
	
}