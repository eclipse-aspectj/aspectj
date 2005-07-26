class C<A,B> { public B getB(A a) { return null; } }

aspect X {
  public List<C> C<D,C>.getBs(D ds) { return null; }
}
