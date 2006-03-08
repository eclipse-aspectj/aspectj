abstract aspect AbstractSystemArchitecture {

	public abstract pointcut inMyApplication();
	
	// more pointcuts below...
	
}

aspect MySystemArchitecture extends AbstractSystemArchitecture {
	
	public pointcut inMyApplication() : within(SomeClass);
	
}

abstract aspect NoDirectlyRunnableClasses<A extends AbstractSystemArchitecture> {
	
	declare warning : execution(public static void main(String[])) &&
	                  A.inMyApplication()
	                : "no directly runnable classes";

}

aspect NoRunnablesInMyApp extends NoDirectlyRunnableClasses<MySystemArchitecture> {
	
}


class SomeClass {

	public static void main(String[] args) {  // CW L30
		System.out.println("hello");
	}

}

class SomeOtherClass {

	public static void main(String[] args) {  // no warning
		System.out.println("hello");
	}

}