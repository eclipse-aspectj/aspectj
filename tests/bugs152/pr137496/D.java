interface P<T> {
  public T pm(T t);
  public String pm2(String t);
}

interface C extends P<String> {
  public void cm();
}

class CImpl implements C {
  public void cm() {}
  public String pm(String s)  { System.err.println(s);return s;}
  public String pm2(String s) { System.err.println(s);return s;}
}

public class D {

  public static void main(String []argv) {
    CImpl test = new CImpl();
    test.pm("foo"); // manifests as 'String pm(String) call' due to type CImpl being used
    test.pm2("foo");
  }
}

aspect X {
  before(): call(* pm(..)) { System.err.println("advice");}
  before(): call(* pm2(..)) { System.err.println("advice2");}
}