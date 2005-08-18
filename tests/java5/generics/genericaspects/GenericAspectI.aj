// here A is modified to implement SimpleI<B> where B is a type var reference
// passed to the generic aspect
import java.util.*;

abstract aspect GenericAspect<A,B> {

  interface SimpleI<L> {}

  declare parents: A implements SimpleI<B>;

  public N SimpleI<N>.m4(N n) { System.err.println(n);return n;}

}

aspect GenericAspectI extends GenericAspect<Base,String> {
  public static void main(String []argv) {
    Base b = new Base();
    String s = b.m4("hello");
    if (!s.equals("hello"))
      throw new RuntimeException("Not hello?? "+s);
  }
}

class Base {}

