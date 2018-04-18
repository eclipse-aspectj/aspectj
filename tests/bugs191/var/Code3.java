public class Code3 {
	public static void main(String []argv) {
		var x = "hello";
		System.out.println(x.getClass());
	}
}

aspect X {
	before(): call(* *.getClass()) && target(String) {
		System.out.println(thisJoinPointStaticPart);
	}
}
