package pkg;

import org.aspectj.lang.JoinPoint;

public aspect A {

	pointcut p() : within(C) && execution(* *(..));
	
	before() : p() {
	}
	
	after(): execution(void printParameters(..)) {
	}
	
	static private void printParameters(JoinPoint jp) {
	}
	
}
