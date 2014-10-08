interface Foo {
	default void printMessage() {
		System.out.println("GW");
	}
}

interface Bar {
	default void printMessage() {
		System.out.println("HW");
	}
}

class FooImpl implements Foo,Bar {
	public void printMessage() {
		Bar.super.printMessage();
	}
}


public class Demo {
	public static void main(String[] args) {
          new FooImpl().printMessage();
	}
}
