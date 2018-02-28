public class Clinit {
	public static void main(String []argv) {
		new Inner();
	}

	static class Inner {}
}

aspect X {
	before(): staticinitialization(Clinit.Inner) {
		System.out.println(thisJoinPointStaticPart.getSignature());
	}
	before(): staticinitialization(Clinit) {
		System.out.println(thisJoinPointStaticPart.getSignature());
	}
}
