// this bridge stuff is handled by the compiler
import java.lang.reflect.*;

abstract class C<A> {
	abstract A next();
}

class D extends C<String> {
	String next() {return "";}
}

public aspect Bridging2 {
	public static void main(String []argv) {
		Util.dumpMethods("D");
	}
}