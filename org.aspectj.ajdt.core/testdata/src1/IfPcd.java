public aspect IfPcd {
	private static int foo = 393;

	before(): execution(void IfPcd.main(..)) && if(foo == 393) {
		System.err.println("Hiya");
		foo++;
		main(null);  // saved from an infinite loop by the if
	}
	
	before(): execution(void IfPcd.main(..)) && if(thisJoinPoint.getThis() == null) {
		System.err.println("!has this: "+ thisJoinPoint.getThis());
	}
	
	before(): execution(void IfPcd.main(..)) && if(thisJoinPointStaticPart.getKind() == "method-execution") {
		System.err.println("is method-exec");
	}
	
	
	before(Object o): execution(void IfPcd.main(..)) && args(o) && if(o != null) {
		System.err.println("got: " + o);
	}
	
	pointcut fun(Object o): args(o) && if(o != null);
	
	before(Object o1, Object o2): execution(void IfPcd.main(..)) && args(o1) && fun(o2) {
		System.err.println("got: " + o2);
	}
	
	before(): execution(void IfPcd.main(..)) && fun(Object) {
		System.err.println("got nothin");
	}
	
	public static void main(String[] args) {
		System.err.println("actual body: " + args);
	}
}

