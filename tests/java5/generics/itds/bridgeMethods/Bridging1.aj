// This bridge stuff is handled by the compiler - because
// D is a subtype of C even though method1 is being overridden,
// a bridge method 'C method1() { method1();}' is generated
// in the subclass D
import java.lang.reflect.*;

class C {
  C method1() {return null;}
}

class D extends C {
  D method1() {return null;}
}

public aspect Bridging1 {
  public static void main(String []argv) {
    Util.dumpMethods("D");
  }
}
