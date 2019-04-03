public class Switch3 {
	public static void main(String[] argv) {
		System.out.println(one(Color.R));
		System.out.println(one(Color.G));
		System.out.println(one(Color.B));
		System.out.println(one(Color.Y));
	}

	public static int one(Color color) {
		int result = switch(color) {
		case R -> foo(0);
		case G -> foo(1);
		case B -> foo(2);
		default -> foo(3);
		};
		return result;
	}
	
	public static final int foo(int i) {
		return i+1;
	}
}

enum Color {
	R, G, B, Y;
}

aspect X {
	int around(): call(* foo(..)) {
		return proceed()*3;
	}
}