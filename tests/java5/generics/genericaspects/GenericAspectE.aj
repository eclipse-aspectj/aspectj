import java.util.*;

// Dumbed down to a normal aspect - as its generic form was failing...
public aspect GenericAspectE { 

  public    void IUtil<Z>.print1() {}
  public    void IUtil<Z>.print2(Z n) {}
  public List<Z> IUtil<Z>.print3() {return null;}

  public static void main(String []argv) {
    Base b = new Base();
    b.print1();
    b.print2("hello");
    List<String> s = b.print3();
  }
}

interface IUtil<X> { }

class Base implements IUtil<String> {}

