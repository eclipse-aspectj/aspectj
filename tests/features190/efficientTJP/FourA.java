public class FourA {
	public static void main(String []argv) {
		new FourA().run();
	}

	public void run() {
		try {
			System.out.println("run() running");
			throw new IllegalStateException();
		} catch (Throwable t) {
			System.out.println("caught something");
		}
	}

}

aspect X {
	before(Throwable t): handler(Throwable) && args(t) {
		System.out.println(thisEnclosingJoinPointStaticPart.getSignature());
	}
}
