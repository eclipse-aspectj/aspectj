// ITD of a method onto a generic inner type - complex example

interface I<P> {
  interface J<Q> {
  }
}

aspect Bang {
 public int I<A>.J<B>.intro(A a,B b) {return 42;}
}

class Impl implements I<Integer> {
  class InnerImpl implements J<String> {
  }
}

public class ExampleE {
  public static void main(String []argv) {
    Impl i = new Impl();
    Impl.InnerImpl j = i.new InnerImpl();
    System.out.println(j.intro(new Integer(5),"foo"));
  }
}