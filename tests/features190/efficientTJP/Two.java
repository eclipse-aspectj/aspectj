public class Two {
	public static void main(String []argv) {
		System.out.println("Two running");
	}
}

aspect X {
	void around(): execution(* main(..)) {
		System.out.println(thisEnclosingJoinPointStaticPart.getSignature());
		proceed();
	}
}
