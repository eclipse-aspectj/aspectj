// Bind the target and pass in the right order

aspect X42 {
  M newM = new M("2");

  void around(M t,String p): call(void M.method(String)) && args(p) && target(t) {
    System.err.println("advice from code aspect");
    proceed( newM , "faked" );
  }

  public static void main(String []argv) {
    M.main(argv);
  }
}



class M {

  String prefix;

  public M(String prefix) { this.prefix = prefix; }

  public static void main( String[] args ) {
    M m = new M("1");
    m.method("real");
  }

  public void method(String s) { System.err.println(prefix+s); }
}
