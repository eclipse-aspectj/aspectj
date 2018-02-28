public class Three {
	public static void main(String []argv) {
		System.out.println("Three running");
		new Three();
		new Three("abc");
		new Three(1,"abc");
	}

	Three() {}
	Three(String s) {}
	Three(int i, String s) {}
}

aspect X {
	void around(): execution(new(..)) && !within(X) {
		System.out.println(thisJoinPointStaticPart.getSignature());
		proceed();
	}
}
