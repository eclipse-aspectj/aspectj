public aspect X {
  declare parents: C extends B;

  public B.new(int i) {
    super(i);
  }
}
