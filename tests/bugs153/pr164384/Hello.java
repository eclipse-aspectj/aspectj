package pkg;

public class Hello {
	
	@MyAnnotation
	public void sayHello() {
		System.out.println("hello");
		int counter = 0;
		for (int i = 0; i < 10; i++) {
			counter = i;
		}
		
	}
	
}
