public aspect AtArgsAspect {
	
	
	pointcut myMethod() : execution(* myMethod(..));
		
	// Exact number of args
	// test 0
	before() : myMethod() && @args(*,*,*,*,*) {
		System.out.print("@args(*,*,*,*,*): ");
		System.out.println(TestingArgsAnnotations.expected() ? "PASS" : "FAIL");
	}
	@org.aspectj.lang.annotation.SuppressAjWarnings before() : myMethod() && !@args(*,*,*,*,*) {
		System.out.print("@args(*,*,*,*,*): ");
		System.out.println(!TestingArgsAnnotations.expected() ? "PASS" : "FAIL");
	}
	
	// One too few
	// test 1
	@org.aspectj.lang.annotation.SuppressAjWarnings before() : myMethod() && @args(*,*,*,*) {
		System.out.print("@args(*,*,*,*): ");
		System.out.println(TestingArgsAnnotations.expected() ? "PASS" : "FAIL");
	}
	before() : myMethod() && !@args(*,*,*,*) {
		System.out.print("@args(*,*,*,*): ");
		System.out.println(!TestingArgsAnnotations.expected() ? "PASS" : "FAIL");
	}
	
	// One too many
	// test 2
	@org.aspectj.lang.annotation.SuppressAjWarnings before() : myMethod() && @args(*,*,*,*,*,*) {
		System.out.print("@args(*,*,*,*,*,*): ");
		System.out.println(TestingArgsAnnotations.expected() ? "PASS" : "FAIL");
	}
	before() : myMethod() && !@args(*,*,*,*,*,*) {
		System.out.print("@args(*,*,*,*,*,*): ");
		System.out.println(!TestingArgsAnnotations.expected() ? "PASS" : "FAIL");
	}

	// Exact number of args + ellipsis
	// test 3
	before() : myMethod() && @args(*,*,..,*,*,*) {
		System.out.print("@args(*,*,..,*,*,*): ");
		System.out.println(TestingArgsAnnotations.expected() ? "PASS" : "FAIL");
	}
	@org.aspectj.lang.annotation.SuppressAjWarnings before() : myMethod() && !@args(*,*,..,*,*,*) {
		System.out.print("@args(*,*,..,*,*,*): ");
		System.out.println(!TestingArgsAnnotations.expected() ? "PASS" : "FAIL");
	}

	// Too few + ellipsis
	// test 4
	before() : myMethod() && @args(*,*,*,..) {
		System.out.print("@args(*,*,*,..): ");
		System.out.println(TestingArgsAnnotations.expected() ? "PASS" : "FAIL");
	}
	@org.aspectj.lang.annotation.SuppressAjWarnings before() : myMethod() && !@args(*,*,*,..) {
		System.out.print("@args(*,*,*,..): ");
		System.out.println(!TestingArgsAnnotations.expected() ? "PASS" : "FAIL");
	}
	
	// Now we get to test some annotations!
	
	// Non-inherited 
	// test 5
	before() : myMethod() && @args(MyAnnotation,..) {
		System.out.print("@args(@MyAnnotation,..): ");
		System.out.println(TestingArgsAnnotations.expected() ? "PASS" : "FAIL");
	}
	before() : myMethod() && !@args(MyAnnotation,..) {
		System.out.print("@args(@MyAnnotation,..): ");
		System.out.println(!TestingArgsAnnotations.expected() ? "PASS" : "FAIL");
	}

	// test 6
	before() : myMethod() && @args(MyAnnotation,*,*,MyAnnotation,*) {
		System.out.print("@args(@MyAnnotation,*,*,@MyAnnotation,*): ");
		System.out.println(TestingArgsAnnotations.expected() ? "PASS" : "FAIL");
	}
	before() : myMethod() && !@args(MyAnnotation,*,*,MyAnnotation,*) {
		System.out.print("@args(@MyAnnotation,*,*,@MyAnnotation,*): ");
		System.out.println(!TestingArgsAnnotations.expected() ? "PASS" : "FAIL");
	}

	// test 7
	before() : myMethod() && @args(MyAnnotation,*,*,MyAnnotation,MyAnnotation) {
		System.out.print("@args(@MyAnnotation,*,*,@MyAnnotation,@MyAnnotation): ");
		System.out.println(TestingArgsAnnotations.expected() ? "PASS" : "FAIL");
	}
	before() : myMethod() && !@args(MyAnnotation,*,*,MyAnnotation,MyAnnotation) {
		System.out.print("@args(@MyAnnotation,*,*,@MyAnnotation,@MyAnnotation): ");
		System.out.println(!TestingArgsAnnotations.expected() ? "PASS" : "FAIL");
	}
	
	// Inherited
	// test 8
	before() : myMethod() && @args(..,MyInheritableAnnotation,*) {
		System.out.print("@args(..,@MyInheritableAnnotation,*): ");
		System.out.println(TestingArgsAnnotations.expected() ? "PASS" : "FAIL");
	}
	@org.aspectj.lang.annotation.SuppressAjWarnings before() : myMethod() && !@args(..,MyInheritableAnnotation,*) {
		System.out.print("@args(..,@MyInheritableAnnotation,*): ");
		System.out.println(!TestingArgsAnnotations.expected() ? "PASS" : "FAIL");
	}

	// test 9
	before() : myMethod() && @args(..,MyInheritableAnnotation,MyInheritableAnnotation) {
		System.out.print("@args(..,@MyInheritableAnnotation,@MyInheritableAnnotation): ");
		System.out.println(TestingArgsAnnotations.expected() ? "PASS" : "FAIL");
	}
	@org.aspectj.lang.annotation.SuppressAjWarnings before() : myMethod() && !@args(..,MyInheritableAnnotation,MyInheritableAnnotation) {
		System.out.print("@args(..,@MyInheritableAnnotation,@MyInheritableAnnotation): ");
		System.out.println(!TestingArgsAnnotations.expected() ? "PASS" : "FAIL");
	}

	// test 10
	before() : myMethod() && @args(..,MyInheritableAnnotation,MyInheritableAnnotation,MyInheritableAnnotation) {
		System.out.print("@args(..,@MyInheritableAnnotation,@MyInheritableAnnotation,@MyInheritableAnnotation): ");
		System.out.println(TestingArgsAnnotations.expected() ? "PASS" : "FAIL");
	}
	before() : myMethod() && !@args(..,MyInheritableAnnotation,MyInheritableAnnotation,MyInheritableAnnotation) {
		System.out.print("@args(..,@MyInheritableAnnotation,@MyInheritableAnnotation,@MyInheritableAnnotation): ");
		System.out.println(!TestingArgsAnnotations.expected() ? "PASS" : "FAIL");
	}

}
