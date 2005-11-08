// pr104220 - adviceexecution joinpoint toString forms

public aspect Pr104220 {
	
	before() : adviceexecution() && !within(Pr104220) {
		System.out.println(thisJoinPoint.getKind());
		System.out.println(thisJoinPoint.toString());
		System.out.println(thisJoinPoint.toShortString());
		System.out.println(thisJoinPoint.toLongString());
	}

	public static void main(String[] args) {
		new C().foo();
	}
	
}

class C {
	
	public void foo() {}
	
}

aspect SomeAspect {
	
	before() : execution(* foo(..)) {}
	
}

