// this bridge stuff is handled by the compiler
// We should get two methods in D, one is next() with return type Object
// and one is next() with return type String
import java.lang.reflect.*;

abstract class C<A> {
	public abstract A next();
}

class D extends C<String> {
	//public String next() {return "";}
}

public aspect BridgingITD2 {
	
	public String D.next() { return ""; }
	
	public static void main(String []argv) {
		Util.dumpMethods("D");
                D d = new D();
                String s = d.next();
	}
}
