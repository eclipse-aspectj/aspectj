class Test
{  
	public static void main(String args[]) {		
		new Test().method();
	}
	public void method() {
		new Test2().method2();
	}
	
	public void method3() {
		new Test2().method3(new Test());
	}
	
	public void method4(Test t) {
		new Test().method4(new Test());
	}
}
class Test2 {
	public void method2() {}
	public void method3(Test t) {}
}
aspect Plain {
	before(Test x):  call(void *.* (..)) && (target(x) || this(x)) {}

	before(Test x):  call(void *.* (..)) && (this(x) || target(x)){}
	
	before(Test x):  call(void *.*(..)) && (this(x) || args(x)) {}
	
	before(Test x):  call(void *.*(..)) && (args(x) || target(x)) {}
}