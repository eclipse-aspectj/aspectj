public class HelloWorldEnumSwitch {

	public static void main(String[] args) {
		switch(TestEnum.A) {
			case A:
				System.out.println("A");
				break;
			case B:
				System.out.println("B");
		}

	}

	public static enum TestEnum {
		A,
		B;

		private TestEnum() {
		}
	}
}

