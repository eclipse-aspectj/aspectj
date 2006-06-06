import java.util.List;

aspect A {

	pointcut p() : execution(* *.*(..));
	
	before() : p() {}
	
	public void MyClass.method() {}
	
	public MyClass.new() {super();}
}

class C {
	
	public C() {}
	
	public void method() {}
	
	public void intMethod(int i) {}
	
	public void stringMethod(String s) {}
	
	public void myClassMethod(MyClass s) {}
	
	public void genericMethod(List<String> l) {}
	
	public void twoArgsMethod(int i, String s) {}
	
	public void genericMethod2(MyGenericClass<String,MyClass> m) {}
	
	public static void main(String[] args) {}
	
	public void multiMethod(String[][] s) {}
	
	public void intArray(int[] i) {}
	
}

class MyClass {
	
	public MyClass(String s) {}
	
}

class MyGenericClass<X,Y> {}
