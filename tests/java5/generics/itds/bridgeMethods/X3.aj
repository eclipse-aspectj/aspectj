public aspect X3 {

  public Object Super3.m() {return null;}

  public static void main(String []argv) {
    Super3 s = new Sub3();
    Integer i = (Integer)s.m();

    Util.checkMethods("Sub3",
      new String[]{
        "java.lang.Object Sub3.m() [BridgeMethod]",
        "java.lang.Integer Sub3.m()"});
  }
}
