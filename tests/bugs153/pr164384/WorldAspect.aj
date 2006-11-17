package pack;

import pkg.MyAnnotation;

public aspect WorldAspect {

	pointcut exec() : execution(@MyAnnotation * *.*(..));
	
	after() returning : exec() {
		System.out.println("world");
	}
	
}
