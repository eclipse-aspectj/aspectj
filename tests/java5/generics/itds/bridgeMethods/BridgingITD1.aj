// Now the implementation is introduced via an ITD, we should
// still get the bridge method in the class D
import java.lang.reflect.*;

class C {
	public C method1() {return null;}
}

class D extends C {
	// D method1() {return null;}
}

public aspect BridgingITD1 {
  public D D.method1() { return null; }
  public static void main(String []argv) {
    Util.dumpMethods("D");
  }
}
