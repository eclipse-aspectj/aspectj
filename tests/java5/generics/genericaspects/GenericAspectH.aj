import java.util.*;

abstract aspect GenericAspect<A> {

  interface SimpleI<N extends Number> {}

  declare parents: A implements SimpleI<String>; // error - String doesnt meet bounds

  public N SimpleI<N>.m4(N n) { System.err.println(n);return n;}

}

aspect GenericAspectH extends GenericAspect<Base> {
  public static void main(String []argv) {
    Base b = new Base();
    String s = b.m4("hello");
    if (!s.equals("hello"))
      throw new RuntimeException("Not hello?? "+s);
  }
}

class Base {}

