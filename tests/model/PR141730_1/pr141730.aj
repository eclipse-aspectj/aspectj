aspect A {
	
	pointcut p() : execution(* *.*(..)) || execution(*.new(..));
	
	before() : p() {
		
	}
	
}

class C {
	
	public C() {}
	
	public void method() {}
	
	public void intMethod(int i) {}
	
	public void stringMethod(String s) {}
	
	public void myClassMethod(MyClass s) {}
	
	public void twoArgsMethod(int i, String s) {}
	
	public static void main(String[] args) {}
	
	public void multiMethod(String[][] s) {}
	
	public void intArray(int[] i) {}
	
}

class MyClass {}
