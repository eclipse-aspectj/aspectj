public aspect X2 {
  public Integer Sub2.m() {return new Integer(42);}

  public static void main(String []argv) {
    Super2 s = new Sub2();
    Integer i = (Integer)s.m();
  }
}
