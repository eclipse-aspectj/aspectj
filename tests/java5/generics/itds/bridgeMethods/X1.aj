public aspect X1 {
  public static void main(String []argv) {
    Super1 s = new Sub1();
    Integer i = (Integer)s.m();
  }
}
