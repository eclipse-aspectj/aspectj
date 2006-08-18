public aspect AnnotationMatcher {
	
	after() returning : initialization(*.new(..)) && @this(Configurable) {
		System.out.println("annotated type initialized");
	}
	
	
}