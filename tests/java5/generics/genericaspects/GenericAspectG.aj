import java.util.*;

abstract aspect GenericAspect<A> {

  interface SimpleI<N extends Number> {}

  declare parents: A implements SimpleI<Integer>;

  public    void SimpleI<N>.m1() {};
  public List<N> SimpleI<N>.m2() {return null;};
  public    void SimpleI<N>.m3(List<N> ln) {};
  public    void SimpleI<N>.m4(N n) {};

}

aspect GenericAspectG extends GenericAspect<Base> {
  public static void main(String []argv) {
    Base b = new Base();

    b.m1();
    List<Integer> ln = b.m2();
    b.m3(new ArrayList<Integer>());
    b.m4(new Integer(5));
  }
}

class Base {}

