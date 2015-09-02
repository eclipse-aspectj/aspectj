package ajtest;

public class TestClass {
	@AjTarget
	private Long test;
	
	public void testMethod() {
		Object o = test;
		System.out.println(o);
	}
}
