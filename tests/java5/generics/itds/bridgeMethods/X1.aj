public aspect X1 {
  public static void main(String []argv) {
    Super1 s = new Sub1();
    Integer i = (Integer)s.m();

    Util.checkMethods("Sub1",
      new String[]{
        "java.lang.Object Sub1.m() [BridgeMethod]",
        "java.lang.Integer Sub1.m()"});
  }
}
