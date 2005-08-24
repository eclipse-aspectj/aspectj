// this bridge stuff is handled by the compiler
import java.lang.reflect.*;

abstract class C<A> {
	abstract A id(A x);
}

class D extends C<String> {
	String id(String s) {return s;}
}

public aspect Bridging3 {
	public static void main(String []argv) {
		Util.dumpMethods("D");
	}
}