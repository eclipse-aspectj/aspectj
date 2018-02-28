public class ThreeA {
	public static void main(String []argv) {
		System.out.println("ThreeA running");
		new ThreeA();
		new ThreeA("abc");
		new ThreeA(1,"abc");
	}

	ThreeA() {}
	ThreeA(String s) {}
	ThreeA(int i, String s) {}
}

aspect X {
	void around(): execution(new(..)) && !within(X) {
		System.out.println(thisEnclosingJoinPointStaticPart.getSignature());
		proceed();
	}
}
