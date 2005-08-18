// lots of errors
import java.util.*;

abstract aspect GenericAspect<A,B> {

  interface SimpleI<L> {}

  declare parents: A implements SimpleI<B>;

  public N SimpleI<N>.m0(N n) { System.err.println(n);return n;}
  public List<N> SimpleI<N>.m1(List<N> ln) { System.err.println(ln);return ln;}
  public N SimpleI<N>.f;
  public List<N> SimpleI<N>.lf;

}

// We are making the decp put SimpleI<Integer> on Base - so all these string
// things below should fail!
aspect GenericAspectM extends GenericAspect<Base,Integer> { 
  public static void main(String []argv) {
    Base b = new Base();
    List<String> ls = new ArrayList<String>();
    String s = b.m0("hello"); // error
    List<String> ls2 = b.m1(ls);// error
    b.f="hello";// error
    b.lf=ls;// error
  }
}

class Base {}

