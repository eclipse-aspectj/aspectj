import java.util.*;

abstract aspect GenericAspect<A,B> {

  interface SimpleI<L> {}

  declare parents: A implements SimpleI<B>;

  public N SimpleI<N>.m0(N n) { System.err.println(n);return n;}
  public List<N> SimpleI<N>.m1(List<N> ln) { System.err.println(ln);return ln;}
  public N SimpleI<N>.f;
  public List<N> SimpleI<N>.lf;

}

aspect GenericAspectL extends GenericAspect<Base,String> { 
  public static void main(String []argv) {
    Base b = new Base();
    List<String> ls = new ArrayList<String>();
    String s = b.m0("hello");
    List<String> ls2 = b.m1(ls);

    b.f="hello";
    b.lf=ls;
  }
}

class Base {}

