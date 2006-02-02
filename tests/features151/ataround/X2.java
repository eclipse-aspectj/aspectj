// Bind the target but forget to pass it on the proceed call

aspect X2 {

  void around(M t,String p): call(void M.method(String)) && args(p) && target(t) {
    System.err.println("advice from code aspect");
    proceed( "faked" ); // X2.java:7 [error] too few arguments to proceed, expected 2
  }

  public static void main(String []argv) {
    M.main(argv);
  }
}



class M {

  String prefix;

  public M(String prefix) { this.prefix = prefix; }

  public static void main( String[] args ) {
    M m = new M(">");
    m.method("real");
  }

  public void method(String s) { System.err.println(s); }
}
