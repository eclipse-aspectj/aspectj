public class PrimitivePatternsSwitch2 {

	public static void main(String[] argv) {
		System.out.println(fn(new Number(1)));
		System.out.println(fn(new Number(2)));
		System.out.println(fn(new Number(3)));
		System.out.println(fn(new Number(4)));
	}

	static String fn(Number n) {
		return switch (n.value()) {
			case int i when isOdd(i) -> "yes";
			case int i -> "no";
		};
	}

	static boolean isOdd(int i) {
		return (i%2)==0;	
	}

}

aspect X {
 	boolean around(int i): call(* isOdd(..)) && args(i) {
 		return false;
 	}
}

class Number {
	private int i;
	Number(int n) { this.i = n; }
	int value() { return i; }
}
