

public class Main {
	public static void main(String[] args) {
		lib.Lib.f(); // bug: unable to resolve lib.Lib after change
	}
}
