// ITD of a method onto a generic inner type - working example

interface I {
  interface J< T > {}
}

aspect Bang {
 public int I.J<T>.intro(T t) {return 42;}
}

class Impl implements I {
  class InnerImpl implements J<String> {
  }
}

public class ExampleA {
  public static void main(String []argv) {
    Impl i = new Impl();
    Impl.InnerImpl j = i.new InnerImpl();
    System.out.println(j.intro("foo"));
  }
}