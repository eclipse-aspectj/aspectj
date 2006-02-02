// Bind the target but pass args in wrong order on proceed

aspect X3 {

  void around(M t,String p): call(void M.method(String)) && args(p) && target(t) {
    System.err.println("advice from code aspect");
    proceed( "faked" , t);
    // X3.java:7 [error] Type mismatch: cannot convert from String to M
    // X3.java:7 [error] Type mismatch: cannot convert from M to String
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
