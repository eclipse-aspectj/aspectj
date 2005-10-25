public aspect X4 {

  declare parents: Sub4 extends Super4;

  public static void main(String []argv) {
    Super4 s = new Sub4();
    Integer i = (Integer)s.m();
    if (i!=42) throw new RuntimeException("Should be 42 but is "+i);
  }
}
