// Bind the this on execution and change it with proceed... works, modifies instance that executes

aspect X8 {
  M newM = new M("2");

  void around(M t,String p): execution(void M.method(String)) && args(p) && this(t) {
    System.err.println("advice from code aspect");
    proceed( newM,"faked" );
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
    m.methodCaller("real");
  }

  public void methodCaller(String param) {
    method(param);
  }

  public void method(String s) { System.err.println(prefix+s); }
}
