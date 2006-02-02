// Bind the target but make it the second arg rather than the first

aspect X6 {
  M newM = new M("2");

  void around(String p,M t): call(void M.method(String)) && args(p) && target(t) {
    System.err.println("advice from code aspect");
    proceed( "faked" , newM);
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
