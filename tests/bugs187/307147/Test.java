package test;

public class Test {

	public Test() {
	}
	
	public static void main(String[] args) {
		Test t = new Test();
		t.function();
		t.itdFunction();
	}
	
	public void function() {
		System.out.println("Normal function");
		privateMethod();
		publicMethod();
	}
	
	private void privateMethod() {
		System.out.println("private method");
	}
	
	public void publicMethod() {
		System.out.println("public method");
	}

}
