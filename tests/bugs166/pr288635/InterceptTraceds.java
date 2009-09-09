public aspect InterceptTraceds {

	before(Anno t) : execution(@Anno * *.*(..)) && @annotation(t) {
		if (t != null) {
			System.out.println("Executing " + thisJoinPoint + " on level " + t.level());
		} else {
			System.out.println("Annotation was null on " + thisJoinPoint);
		}
	}
	
}
