package org.tests.atann;

public aspect InterceptTraceds {

	before(Traced t) : execution(@Traced * *.*(..)) && @annotation(t) {
		if (t != null) {
			System.out.println("Executing " + thisJoinPoint + " on level " + t.level());
		} else {
			System.out.println("Annotation was null on " + thisJoinPoint);
		}
	}
	
}
