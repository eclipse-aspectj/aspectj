public class One {
	public static void main(String []argv) {
		System.out.println("One running");
	}
}

aspect X {
	void around(): execution(* main(..)) {
		System.out.println(thisJoinPoint.getSignature());
		proceed();
	}
}
