interface P<T> {
  public T pm(T t);
//  public String pm2(String t);
}

interface C extends P<String> {
  public void cm();
}

class CImpl implements C {
  public void cm() {}
  public String pm(String s)  { System.err.println(s);return s;}
//  public String pm2(String s) { return s;}
}

public class H {

  public static void main(String []argv) {
    C test = new CImpl();
    test.pm("foo"); // manifests as 'Object pm(Object) call' due to type C being used
//    test.pm2("foo");
  }
}

aspect X {
  Object around(): call(* pm(..)) { System.err.println("advice"); return null;}
//  before(): call(* pm2(..)) {}
}