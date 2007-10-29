// ITD of a method onto a generic inner inner type

interface I {
  interface J {
	  interface K<T> {}
  }
}

aspect Bang {
 public int I.J.K<T>.intro(T t) {return 42;}
}

class Impl implements I {
  class InnerImpl implements J {
	  class InnerInnerImpl implements K<String> {}
  }
}

public class ExampleC {
  public static void main(String []argv) {
    Impl i = new Impl();
    Impl.InnerImpl j = i.new InnerImpl();
    Impl.InnerImpl.InnerInnerImpl k = j.new InnerInnerImpl();
    System.out.println(k.intro("foo"));
  }
}