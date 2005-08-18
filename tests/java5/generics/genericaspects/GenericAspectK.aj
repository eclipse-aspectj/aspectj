import java.util.*;

abstract aspect GenericAspect<A,B> {

  interface SimpleI<L extends Number> {}

  declare parents: A implements SimpleI<B>;

  public N SimpleI<N>.m4(N n) { System.err.println(n);return n;}

}

aspect GenericAspectI extends GenericAspect<Base,String> { // error, String doesnt meet bounds in SimpleI<> in the generic aspect
  public static void main(String []argv) {
    Base b = new Base();
    String s = b.m4("hello");
    if (!s.equals("hello"))
      throw new RuntimeException("Not hello?? "+s);
  }
}

class Base {}

