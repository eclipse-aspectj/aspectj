package a.b.c;

public class Code {
	public static void main(String []argv) {
		new Code().new PublicInner().run();
		new Code().new DefaultInner().run();
		new Code().new PrivateInner().run();
	}
	
	public class PublicInner implements Runnable {
		public void run() {
			System.out.println("PublicInner.run()");
		}
	}

	class DefaultInner implements Runnable {
		public void run() {
			System.out.println("DefaultInner.run()");
		}
	}

	private class PrivateInner implements Runnable {
		public void run() {
			System.out.println("PrivateInner.run()");
		}
	}

	public void run() {
		System.out.println("Code (outer public class).run()");
	}
}
