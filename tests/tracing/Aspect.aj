public aspect Aspect {
	before () : within(HelloWorld) {
		System.err.println(thisJoinPoint);
	}
}