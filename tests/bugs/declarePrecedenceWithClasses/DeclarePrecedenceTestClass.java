//Bug 53012
// DP contains a declare precedence statement that mentions classes

public class DeclarePrecedenceTestClass {
	public static void main(String[] args) {
		System.out.println("hello");
	}
}
aspect DP {
	declare precedence: DeclarePrecedenceTestClass, DP;
	
	before() : staticinitialization(DeclarePrecedenceTestClass) {
		System.out.println("ok"); 
	}
}
