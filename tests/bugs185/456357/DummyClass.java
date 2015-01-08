public class DummyClass {
	@LogMe
	public void doSomething() {
		SampleUtil sampleUtil = new SampleUtil();
		// pass null for simplicity !
		sampleUtil.sampleMethod(null);
		System.out.println("Do Something");
	}

	public static void main(String[] args) {
		new DummyClass().doSomething();
	}
}
