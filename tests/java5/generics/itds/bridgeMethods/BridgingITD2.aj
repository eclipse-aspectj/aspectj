// this bridge stuff is handled by the compiler
import java.lang.reflect.*;

abstract class C<A> {
	public abstract A next();
}

class D extends C<String> {
	//String next() {return "";}
}

public aspect BridgingITD2 {
	
	public String D.next() { return ""; }
	
	public static void main(String []argv) {
		Util.dumpMethods("D");
 C c = new D();
 String s = c.next();
	}
}
