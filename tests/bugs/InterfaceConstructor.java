interface I { }

public class InterfaceConstructor implements I {
	public static void main(String[] args) {
		new InterfaceConstructor();
	}
}

aspect NoSuchJP {
	before(): execution(I.new(..)) { // error expected
		// No constructor-execution on interface type
	}
	
	before(): execution(I+.new(..)) { // no error
		// This is OK, as there is a +
	}
	
}