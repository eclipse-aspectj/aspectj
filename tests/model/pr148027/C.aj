package pkg;

public class C {

	pointcut pointcutInClass() : execution(void cMethod());
	
	public void cMethod() {
		
	}
}
