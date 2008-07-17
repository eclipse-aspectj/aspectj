package test;

public class TestIt {
	public static void main(String[] args) {
		Sub s = new Sub(3, "testValue", "Desc", 17);
		s.setValue("another value");
		s.setDescription("blue");
		System.out.println("done.");
	}
}
