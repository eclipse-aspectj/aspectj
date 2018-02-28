public class Advice {
	public static void main(String []argv) {
	}
}

aspect X {
	before(): execution(* main(..)) {}
}

aspect Y {
	before(): adviceexecution() && within(X) {
		System.out.println("tjp:"+thisJoinPointStaticPart.getSignature());
	}

	before(): adviceexecution() && within(X) {
		System.out.println("tejp:"+thisEnclosingJoinPointStaticPart.getSignature());
	}

}
