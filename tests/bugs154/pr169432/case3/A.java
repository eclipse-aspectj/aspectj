
// Now C1 and C2 implement the interface

import org.aspectj.lang.annotation.*;

public class A {
  public static void main(String []argv) {
    C1 c1 = new C1();
    System.out.println("C1.m() returns "+((NonMarkerInterface)c1).m());   
    C2 c2 = new C2();
    System.out.println("C2.m() returns "+((NonMarkerInterface)c2).m());   
  }
}

@Aspect class X {
  @DeclareParents(value="C*")
  public NonMarkerInterface nmi;
}

interface NonMarkerInterface {
  public int m();
}

class Y implements NonMarkerInterface {
  public Y() {}
  public int m() { return 43;}
}

class C1 {
  public int m() { return 1;}

}

class C2 {
  public int m() { return 2;}
}
