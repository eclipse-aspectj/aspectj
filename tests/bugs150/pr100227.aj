class Outer {
  class Inner {public void p() {System.err.println("Outer.Inner.p() executing");} }
  public void m() { new Inner().p(); }
}

class Generic_Outer<T> {
  class Inner {public void p() {System.err.println("Generic_Outer.Inner.p() executing");} }

  public void m() { new Inner().p(); }
}

aspect Injector {
  int Outer.outer = 1;
  int Outer.Inner.inner = 2;
  int Generic_Outer.outer = 3; 
  int Generic_Outer.Inner.inner = 4;

  before(Object o): execution(* p()) && this(o) {
    if (o instanceof Outer.Inner) {
      System.err.println("Outer.Inner.inner="+((Outer.Inner)o).inner);
    }
    if (o instanceof Generic_Outer.Inner) {
      System.err.println("Generic_Outer.Inner.inner="+((Generic_Outer.Inner)o).inner);
    }
  }
}

public class pr100227 {
  public static void main(String []argv) {
    new Outer().m();
    new Generic_Outer<String>().m();
  }
}
