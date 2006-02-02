// Bind the this and target on call and change it with proceed... but subset arguments in advice

aspect X11 {
  M newM2 = new M("2");
  M newM3 = new M("3");

  void around(M t,String p,M t2): call(void M.method(..)) && args(*,p,*) && this(t) && target(t2) {
    System.err.println("advice from code aspect");
    proceed( newM2,"_" , newM3);
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
    m.methodCaller("x","y","z");
  }

  public void methodCaller(String param,String param2,String param3) {
    method(param,param2,param3);
  }

  public void method(String a,String b,String c) { System.err.println(prefix+a+b+c); }
}
