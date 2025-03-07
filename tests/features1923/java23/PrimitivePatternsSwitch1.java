public class PrimitivePatternsSwitch1 {

	public static void main(String[] argv) {
		System.out.println(fn(new Number(1)));
		System.out.println(fn(new Number(2)));
		System.out.println(fn(new Number(140)));
		System.out.println(fn(new Number(10040)));
	}

	static String fn(Number n) {
		return switch (n.value()) {
			case 1 -> "one";
			case 2 -> "two";
			case int i when (i >= 100 && i <1000) -> "many";
			case int i -> "lots";
		};
	}
}

class Number {
	private int i;
	Number(int n) { this.i = n; }
	int value() { return i; }
}
