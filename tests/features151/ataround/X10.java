// Bind the this and target on call and change it with proceed... 

aspect X10 {
  M newM2 = new M("2");
  M newM3 = new M("3");

  void around(M t,String p,M t2): call(void M.method(String)) && args(p) && this(t) && target(t2) {
    System.err.println("advice from code aspect");
    proceed( newM2,"faked" , newM3);
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
