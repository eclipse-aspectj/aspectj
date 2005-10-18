import java.lang.annotation.*;
import java.lang.ref.*;
import java.util.*;

public class Case7 {

  public static void main(String []argv) {
    String  s = "hello";
    Integer i = 35;
    List    l = new ArrayList();
    List<String> ls = new ArrayList<String>();
    List<Number> ln = new ArrayList<Number>();
    List<List>   ll = new ArrayList<List>();

    A a = new A();
    a.setN(ls,s);
    String s2 = a.getN(ls);
    System.err.println("in="+s+" out="+s2);

    B b = new B();
    b.setN(ln,i);
    System.err.println("in="+i+" out="+b.getN(ln));

    C c = new C();
    c.setN(ll,l);
    List l2 = c.getN(ll);
    System.err.println("in="+l+" out="+l2);

  }

}


interface I<N> {
  N getN(List<N> ns);

  void setN(List<N> ns,N n);
}

aspect X {
  Q I<Q>.value;

  public P I<P>.getN(List<P> ps) {
    return value;
  }

  public void I<Q>.setN(List<Q> ns,N n) {
    this.value = n;
  }

  declare parents : A implements I<String>;
  declare parents : B implements I<Number>;
  declare parents : C implements I<List>;
}


class A { }
class B { }
class C { }
