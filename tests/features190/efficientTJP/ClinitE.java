public class ClinitE {
	public static void main(String []argv) {
		new Inner();
	}

	static class Inner {}
}

aspect X {
	before(): staticinitialization(ClinitE.Inner) {
		System.out.println(thisEnclosingJoinPointStaticPart.getSignature());
	}
	before(): staticinitialization(ClinitE) {
		System.out.println(thisEnclosingJoinPointStaticPart.getSignature());
	}
}
