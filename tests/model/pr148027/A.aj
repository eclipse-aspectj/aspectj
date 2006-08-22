package pkg;

public aspect A {

	before() : C.pointcutInClass() {	
	}
	
	pointcut pointcutInAspect() : execution(void aMethod());
	
	before() : pointcutInAspect() {
	}
	
	public void aMethod() {
	}
}
