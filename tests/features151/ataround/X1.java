// Simple - don't attempt to alter target for proceed, just change the arg

aspect X1 {

  void around(String p): call(void M.method(String)) && args(p) {
    System.err.println("advice from code aspect");
    proceed( "faked" );
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
