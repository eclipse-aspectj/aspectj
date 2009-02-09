public class Test3 {
	public void m0() {
		synchronized (this) {
			synchronized ("Hello") {
				System.out.println("Hello World");
			}

		}
	}
}
