// this bridge stuff is handled by the compiler
// Similar to previous type var test but now the String parameter should
// be Object in the bridge method
import java.lang.reflect.*;

abstract class C<A> {
	public abstract A id(A x);
}

class D extends C<String> {
	//public String id(String s) {return s;}
}

public aspect BridgingITD3 {
	public String D.id(String s) {return s;}
	public static void main(String []argv) {
		Util.dumpMethods("D");
	}
}
