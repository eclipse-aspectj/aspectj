public class Switch1 {
	public static void main(String[] argv) {
		System.out.println(one(Color.R));
		System.out.println(one(Color.G));
		System.out.println(one(Color.B));
		System.out.println(one(Color.Y));
	}

	public static int one(Color color) {
		int result = switch(color) {
		case R -> 0;
		case G -> 1;
		case B -> 2;
		default -> 3;
		};
		return result;
	}
}

enum Color {
	R, G, B, Y;
}