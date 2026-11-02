public class Anonymous {

	// anonymous classes that declare fields but no methods

	Object plainField = new Object() {
		String field;
	};

	Object annotatedField = new Object() {
		@Marker(message = "test message")
		String field;
	};

	Object initializerAndField = new Object() {
		{
			System.out.println("initialized");
		}
		int count;
	};

	public static void main(String[] args) {
		new Anonymous();
		System.out.println("done");
	}
}
