package de.test;

public aspect MyAspect {

	before(): execution(void MyMain.sayHello(..)) {
		System.out.println("before: " + thisJoinPoint.getSignature());
	}
}
