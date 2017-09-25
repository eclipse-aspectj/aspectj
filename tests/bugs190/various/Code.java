public class Code {
	public static void main(String []argv) {
		System.out.println("running");
	}
}

aspect X{
	before(): call(* println(..))  && !within(X) {
		System.out.println(thisJoinPoint);
	}
}
