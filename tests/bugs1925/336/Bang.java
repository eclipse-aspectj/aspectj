public class Bang {

public static void main(String[] argv) {
  new Bang().m("a",1,"b");
}

  public int m(String a, int i, String b) {
    return 42;
  }

}

aspect X {
  int around(String a, int b, String d): execution(* m(..)) && args(a,b,d) {
	return proceed(a,b,d);
  }
}
