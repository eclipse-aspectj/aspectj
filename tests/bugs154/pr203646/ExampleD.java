// ITD of a method onto a generic inner type - working example

interface I {
  interface J<T> {
	  interface K {}
  }
}

aspect Bang {
 public int I.J<P>.intro(P t) {return 42;}
}

class Impl implements I {
  class InnerImpl implements J<String> {
	  class InnerInnerImpl implements K {}
  }
}

public class ExampleD {
  public static void main(String []argv) {
    Impl i = new Impl();
    Impl.InnerImpl j = i.new InnerImpl();
    Impl.InnerImpl.InnerInnerImpl k = j.new InnerInnerImpl();
    System.out.println(j.intro("foo"));
  }
}