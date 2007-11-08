
// Should error, the types C1 and C2 don't implement the interface and no defaultImpl was supplied

import org.aspectj.lang.annotation.*;

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

}

class C2 {

}
