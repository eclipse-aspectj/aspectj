interface P<T> {
  public T pm(T t);
}

interface C extends P<String> {
  public void cm();
}

class CImpl implements C {
  public void cm() {}
  public String pm(String s)  { System.err.println(s);return s;}
}

public class E {

  public static void main(String []argv) {
    C test = new CImpl();
    test.pm("foo"); // manifests as 'Object pm(Object) call' due to type C being used
  }
}

aspect X {
  before(): call(* pm(String)) { System.err.println("advice");} // matches?
}