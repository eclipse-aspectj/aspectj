public class NonSerializableTest {

	private int i;

	public void run () {
		System.out.println("NonSerializableTest.run()");
		i++;
	}
	
	public static void main (String[] args) throws Exception {
		NonSerializableTest test = new NonSerializableTest();
		test.run();
	}
}