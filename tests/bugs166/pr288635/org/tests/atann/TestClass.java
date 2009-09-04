package org.tests.atann;

public class TestClass {

	@Traced
	public String doAnnotated() {
		return "annotated";
	}
	
	public int doITDAnnotation() {
		return 1;
	}
	
	public static void main(String[] args) {
		TestClass tc = new TestClass();
		tc.doAnnotated();
		tc.doITDAnnotation();
		tc.doAnother();
	}
	
}
