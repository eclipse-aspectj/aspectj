
public class StaticInitCE {
   public static void main(String[] args) {
      new C();
   } 
}

class C { C() { } }

aspect A {
	// PR#458
    void around(): staticinitialization(C) {
       proceed();
    }
	// PR#490
    void around(): initialization(C.new()) {
       proceed();
    }
}
