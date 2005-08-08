class Base<N extends Number> {

  public List<N> f1;

  public void m1(List<N> ns) {}

}

aspect X {

  public List<Z> Base<Z>.f2;

  public void Base<Z>.m1(List<Z> ns) {}

}

