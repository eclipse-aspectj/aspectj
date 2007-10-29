// ITD of a method onto a generic inner type - complex example

class Goo {}

interface I {
  interface J<Q extends Goo> {
  }
}

aspect Bang {
 public int I.J.intro(String a,Integer b) {return 42;}
}

class Impl implements I {
	class InnerImpl implements J {
	}
}

public class ExampleF {
  public static void main(String []argv) {
    Impl i = new Impl();
    Impl.InnerImpl j = i.new InnerImpl();
    System.out.println(j.intro("o",new Integer(3)));
  }
}