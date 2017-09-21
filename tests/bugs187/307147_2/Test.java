package test;

public class Test {

	public Test() {
	}
	
	public static void main(String[] args) {
		Test t = new Test();
		t.itdFunction();
	}
	
	private void privateMethod(String xxx) {
		System.out.println("hello "+xxx);
	}
}
